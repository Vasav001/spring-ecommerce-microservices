package com.vasav.ecom.catalogservice;

import com.jayway.jsonpath.JsonPath;
import com.vasav.ecom.catalogservice.config.TestJwts;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProductApiTests extends AbstractIntegrationTest {

    private ResultActions putProduct(UUID id, String conditionHeader, String conditionValue, String body) throws Exception {
        var request = put(PRODUCTS + "/" + id).with(TestJwts.admin())
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (conditionHeader != null) {
            request.header(conditionHeader, conditionValue);
        }
        return mvc.perform(request);
    }

    // ---------- create ----------

    @Test
    void createReturns201WithEtagAndAuditFields() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();

        putProduct(id, "If-None-Match", "*", productJson(sku, null, List.of()))
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.status").value("DRAFT"))   // default when status omitted
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.version").value(0));

        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM product WHERE id = ?", id);
        assertThat(row.get("created_by")).isEqualTo(TestJwts.ADMIN_SUB);
        assertThat(row.get("updated_by")).isEqualTo(TestJwts.ADMIN_SUB);
        assertThat(row.get("created_at")).isNotNull();
        assertThat(row.get("updated_at")).isNotNull();
    }

    @Test
    void createWithExistingIdIs412() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");
        putProduct(id, "If-None-Match", "*", productJson(uniqueSku(), "DRAFT", List.of()))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.status").value(412))
                .andExpect(jsonPath("$.message").value("Product already exists"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void putWithoutConditionalHeaderIs428() throws Exception {
        putProduct(UUID.randomUUID(), null, null, productJson(uniqueSku(), "DRAFT", List.of()))
                .andExpect(status().isPreconditionRequired())
                .andExpect(jsonPath("$.status").value(428));
    }

    @Test
    void activeSkuClashIs409ButInactiveDuplicateIsAllowed() throws Exception {
        String sku = uniqueSku();
        createProduct(UUID.randomUUID(), sku, "ACTIVE");
        createProduct(UUID.randomUUID(), sku, "DRAFT");      // partial unique index: only ACTIVE must be unique

        UUID clashing = UUID.randomUUID();
        putProduct(clashing, "If-None-Match", "*", productJson(sku, "ACTIVE", List.of()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        assertThat(count("SELECT count(*) FROM product WHERE id = ?", clashing)).isZero();
    }

    // ---------- update ----------

    @Test
    void updateWithCurrentVersionIs200AndBumpsVersion() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        createProduct(id, sku, "DRAFT");

        putProduct(id, "If-Match", "\"0\"", productJson(sku, "ACTIVE", List.of()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.version").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // weak ETag form is accepted too
        putProduct(id, "If-Match", "W/\"1\"", productJson(sku, "ARCHIVED", List.of()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"2\""));
    }

    @Test
    void updateWithStaleVersionIs412() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        createProduct(id, sku, "DRAFT");
        putProduct(id, "If-Match", "\"0\"", productJson(sku, "ACTIVE", List.of())).andExpect(status().isOk());

        putProduct(id, "If-Match", "\"0\"", productJson(sku, "ARCHIVED", List.of()))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void identicalPutKeepsVersionButStillAppendsOutboxRow() throws Exception {
        // documents current behaviour: Hibernate skips the UPDATE (nothing dirty) so @Version stays,
        // but the service still emits ProductUpserted with the same version
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        createProduct(id, sku, "DRAFT");
        putProduct(id, "If-Match", "\"0\"", productJson(sku, "DRAFT", List.of()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"0\""));
        assertThat(outboxRows(id)).hasSize(2);
    }

    @Test
    void updateUnknownProductIs404() throws Exception {
        putProduct(UUID.randomUUID(), "If-Match", "\"0\"", productJson(uniqueSku(), "DRAFT", List.of()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void malformedIfMatchIs400() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");
        putProduct(id, "If-Match", "\"abc\"", productJson(uniqueSku(), "DRAFT", List.of()))
                .andExpect(status().isBadRequest());
    }

    // ---------- read ----------

    @Test
    void getReturnsEtagAndUnknownIs404() throws Exception {
        UUID id = UUID.randomUUID();
        createProduct(id, uniqueSku(), "DRAFT");

        mvc.perform(get(PRODUCTS + "/" + id).with(TestJwts.admin()))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.id").value(id.toString()));

        mvc.perform(get(PRODUCTS + "/" + UUID.randomUUID()).with(TestJwts.admin()))
                .andExpect(status().isNotFound());
        mvc.perform(get(PRODUCTS + "/not-a-uuid").with(TestJwts.admin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listIsPagedFilteredAndSorted() throws Exception {
        String prefix = "PAGE-" + UUID.randomUUID().toString().substring(0, 6);
        for (String suffix : List.of("c", "a", "b")) {
            createProduct(UUID.randomUUID(), prefix + "-" + suffix, "ARCHIVED");
        }

        MvcResult result = mvc.perform(get(PRODUCTS).with(TestJwts.admin())
                        .param("status", "ARCHIVED").param("page", "0").param("size", "2").param("sort", "sku,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("ARCHIVED"))))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(3)))
                .andReturn();
        List<String> skus = JsonPath.read(body(result), "$.content[*].sku");
        assertThat(skus).isSorted();

        // a status nobody else uses in the suite would be needed for exact totals, so check the filter instead
        mvc.perform(get(PRODUCTS).with(TestJwts.admin()).param("status", "ARCHIVED").param("size", "100"))
                .andExpect(jsonPath("$.content[*].sku", hasItems(prefix + "-a", prefix + "-b", prefix + "-c")));
    }

    @Test
    void listWithUnknownSortPropertyOrBadStatusIs400() throws Exception {
        mvc.perform(get(PRODUCTS).with(TestJwts.admin()).param("sort", "doesNotExist,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(get(PRODUCTS).with(TestJwts.admin()).param("status", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    // ---------- validation ----------

    @Test
    void invalidBodyIs400WithFieldErrors() throws Exception {
        String body = toJson(Map.of("sku", "", "price", "-1", "category", "x".repeat(101)));
        putProduct(UUID.randomUUID(), "If-None-Match", "*", body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.sku").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.category").exists())
                .andExpect(jsonPath("$.validationErrors.price").exists());
    }

    @Test
    void malformedJsonIs400() throws Exception {
        putProduct(UUID.randomUUID(), "If-None-Match", "*", "{not json")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---------- image refs ----------

    @Test
    void imageRefRulesAreEnforcedWith422() throws Exception {
        UUID id = UUID.randomUUID();
        String own = "cloudinary://products/" + id + "/a";

        List<List<String>> invalid = List.of(
                IntStream.range(0, 11).mapToObj(i -> own + i).toList(),                // > 10 images
                List.of("cloudinary://products/" + UUID.randomUUID() + "/x"),            // other product's folder
                List.of("s3://products/" + id + "/x"),                                   // unknown scheme
                List.of("http://example.com/x.jpg"),                                     // plain http
                List.of("products/" + id + "/x"));                                       // no scheme
        for (List<String> refs : invalid) {
            putProduct(id, "If-None-Match", "*", productJson(uniqueSku(), "DRAFT", refs))
                    .andExpect(status().isUnprocessableContent())
                    .andExpect(jsonPath("$.status").value(422));
        }
        assertThat(count("SELECT count(*) FROM product WHERE id = ?", id)).isZero();

        List<String> valid = new ArrayList<>(IntStream.range(0, 9).mapToObj(i -> own + i).toList());
        valid.add("https://cdn.example.com/x.jpg");
        putProduct(id, "If-None-Match", "*", productJson(uniqueSku(), "DRAFT", valid))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.images", hasSize(10)))
                .andExpect(jsonPath("$.images[0].ref").value(own + "0"))
                .andExpect(jsonPath("$.images[0].url")
                        .value("https://res.cloudinary.com/test-cloud/image/upload/products/" + id + "/a0"))
                .andExpect(jsonPath("$.images[9].url").value("https://cdn.example.com/x.jpg"));
    }

    // ---------- outbox rows ----------

    @Test
    void eachSuccessfulWriteAppendsExactlyOneOutboxRow() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        createProduct(id, sku, "DRAFT");
        assertThat(outboxRows(id)).hasSize(1);

        putProduct(id, "If-Match", "\"0\"", productJson(sku, "ACTIVE", List.of())).andExpect(status().isOk());
        List<Map<String, Object>> rows = outboxRows(id);
        assertThat(rows).hasSize(2);
        assertThat(rows).allSatisfy(row -> {
            assertThat(row.get("topic")).isEqualTo("catalog.products");
            assertThat(row.get("event_type")).isEqualTo("ProductUpserted");
            assertThat(row.get("event_version")).isEqualTo(1);
        });
        String payload = (String) rows.get(1).get("payload");
        assertThat((Object) JsonPath.read(payload, "$.version")).isEqualTo(1);
        assertThat((Object) JsonPath.read(payload, "$.status")).isEqualTo("ACTIVE");
        assertThat((Object) JsonPath.read(payload, "$.price")).isEqualTo("19.99");   // BigDecimal as string
    }

    @Test
    void failedWritesLeaveNoOutboxRows() throws Exception {
        UUID id = UUID.randomUUID();
        String sku = uniqueSku();
        createProduct(id, sku, "ACTIVE");
        int before = outboxRows(id).size();

        putProduct(id, "If-None-Match", "*", productJson(sku, "ACTIVE", List.of()))
                .andExpect(status().isPreconditionFailed());                                      // exists
        putProduct(id, "If-Match", "\"7\"", productJson(sku, "ACTIVE", List.of()))
                .andExpect(status().isPreconditionFailed());                                      // stale
        putProduct(id, "If-Match", "\"0\"", productJson(sku, "ACTIVE", List.of("ftp://nope")))
                .andExpect(status().isUnprocessableContent());                                    // bad image
        putProduct(id, "If-Match", "\"0\"", toJson(Map.of("sku", "")))
                .andExpect(status().isBadRequest());                                              // validation
        assertThat(outboxRows(id)).hasSize(before);

        // 409 is raised by the unique index at flush time
        UUID clashing = UUID.randomUUID();
        putProduct(clashing, "If-None-Match", "*", productJson(sku, "ACTIVE", List.of()))
                .andExpect(status().isConflict());
        assertThat(outboxRows(clashing)).isEmpty();
    }
}
