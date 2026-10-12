package com.vasav.ecom.catalogservice;

import com.jayway.jsonpath.JsonPath;
import com.vasav.ecom.catalogservice.config.TestJwts;
import com.vasav.ecom.catalogservice.events.KafkaTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Duration;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReviewApiTests extends AbstractIntegrationTest {

    private static final String REVIEWS = "/api/v1/reviews";

    private static String newUser() {
        return "user-" + UUID.randomUUID();
    }

    private ResultActions createReview(RequestPostProcessor auth, UUID productId, int rating, String comment)
            throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("productId", productId);
        body.put("rating", rating);
        body.put("comment", comment);
        return mvc.perform(post(REVIEWS).with(auth).contentType(MediaType.APPLICATION_JSON).content(toJson(body)));
    }

    private UUID reviewAs(String user, UUID productId, int rating) throws Exception {
        recordPurchase(user, productId);
        MvcResult result = createReview(TestJwts.customer(user), productId, rating, "ok")
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(json(result, "$.id"));
    }

    private ResultActions updateReview(RequestPostProcessor auth, UUID reviewId, String ifMatch, int rating)
            throws Exception {
        MockHttpServletRequestBuilder request = put(REVIEWS + "/" + reviewId).with(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(Map.of("rating", rating, "comment", "updated")));
        if (ifMatch != null) {
            request.header("If-Match", ifMatch);
        }
        return mvc.perform(request);
    }

    private Map<String, Object> rating(UUID productId) {
        return jdbc.queryForMap("SELECT rating_count, rating_sum, version FROM product_rating WHERE product_id = ?",
                productId);
    }

    private List<String> ratingEvents(UUID productId) {
        return outboxRows(productId).stream()
                .filter(r -> KafkaTopics.CATALOG_PRODUCT_RATINGS.equals(r.get("topic")))
                .map(r -> (String) r.get("payload"))
                .toList();
    }

    // ---------- eligibility ----------

    @Test
    void eligibilityReportsBothReasons() throws Exception {
        UUID product = activeProduct();
        String user = newUser();

        mvc.perform(get(REVIEWS + "/eligibility").param("productId", product.toString()).with(TestJwts.customer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canReview").value(false))
                .andExpect(jsonPath("$.reason").value("NOT_PURCHASED"));

        recordPurchase(user, product);
        mvc.perform(get(REVIEWS + "/eligibility").param("productId", product.toString()).with(TestJwts.customer(user)))
                .andExpect(jsonPath("$.canReview").value(true));

        createReview(TestJwts.customer(user), product, 4, "nice").andExpect(status().isCreated());
        mvc.perform(get(REVIEWS + "/eligibility").param("productId", product.toString()).with(TestJwts.customer(user)))
                .andExpect(jsonPath("$.canReview").value(false))
                .andExpect(jsonPath("$.reason").value("ALREADY_REVIEWED"));
    }

    // ---------- create ----------

    @Test
    void createReturns201WithShortAuthorNameEtagAndLocation() throws Exception {
        UUID product = activeProduct();
        String user = newUser();
        recordPurchase(user, product);

        MvcResult result = createReview(TestJwts.user(user, "Grace", "Hopper", "CUSTOMER"), product, 5, "great")
                .andExpect(status().isCreated())
                .andExpect(header().string("ETag", "\"0\""))
                .andExpect(jsonPath("$.authorName").value("Grace H."))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.productId").value(product.toString()))
                .andReturn();
        assertThat(result.getResponse().getHeader("Location")).isEqualTo(REVIEWS + "/" + json(result, "$.id"));

        assertThat(rating(product)).containsEntry("rating_count", 1).containsEntry("rating_sum", 5)
                .containsEntry("version", 1L);
    }

    @Test
    void authorNameFallbacks() throws Exception {
        UUID product = activeProduct();
        String firstOnly = newUser();
        String anonymous = newUser();
        recordPurchase(firstOnly, product);
        recordPurchase(anonymous, product);

        createReview(TestJwts.user(firstOnly, "Linus", null, "CUSTOMER"), product, 3, null)
                .andExpect(jsonPath("$.authorName").value("Linus"));
        createReview(TestJwts.user(anonymous, null, null, "CUSTOMER"), product, 3, null)
                .andExpect(jsonPath("$.authorName").value("Customer"));
    }

    @Test
    void createErrorCases() throws Exception {
        UUID product = activeProduct();
        String user = newUser();

        createReview(TestJwts.customer(user), product, 4, null)
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));     // not purchased

        recordPurchase(user, product);
        createReview(TestJwts.customer(user), product, 4, null).andExpect(status().isCreated());
        createReview(TestJwts.customer(user), product, 2, null)
                .andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));      // already reviewed

        UUID draft = UUID.randomUUID();
        createProduct(draft, uniqueSku(), "DRAFT");
        recordPurchase(user, draft);
        createReview(TestJwts.customer(user), draft, 4, null).andExpect(status().isNotFound());    // not ACTIVE
        createReview(TestJwts.customer(user), UUID.randomUUID(), 4, null)
                .andExpect(status().isNotFound());                                                 // no product

        createReview(TestJwts.customer(user), product, 6, null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.rating").exists());

        // failed creates changed nothing
        assertThat(rating(product)).containsEntry("rating_count", 1);
        assertThat(ratingEvents(product)).hasSize(1);
        assertThat(count("SELECT count(*) FROM product_rating WHERE product_id = ?", draft)).isZero();
    }

    // ---------- update ----------

    @Test
    void updateAppliesRatingDelta() throws Exception {
        UUID product = activeProduct();
        String user = newUser();
        UUID review = reviewAs(user, product, 5);

        updateReview(TestJwts.customer(user), review, "\"0\"", 2)
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", "\"1\""))
                .andExpect(jsonPath("$.rating").value(2))
                .andExpect(jsonPath("$.comment").value("updated"));
        assertThat(rating(product)).containsEntry("rating_count", 1).containsEntry("rating_sum", 2)
                .containsEntry("version", 2L);

        // same rating: comment-only change, no aggregate change and no event
        updateReview(TestJwts.customer(user), review, "\"1\"", 2).andExpect(status().isOk());
        assertThat(rating(product)).containsEntry("version", 2L);
        assertThat(ratingEvents(product)).hasSize(2);
    }

    @Test
    void updateErrorCases() throws Exception {
        UUID product = activeProduct();
        String author = newUser();
        UUID review = reviewAs(author, product, 4);

        updateReview(TestJwts.customer(author), review, null, 3)
                .andExpect(status().isPreconditionRequired()).andExpect(jsonPath("$.status").value(428));
        updateReview(TestJwts.customer(author), review, "\"9\"", 3)
                .andExpect(status().isPreconditionFailed()).andExpect(jsonPath("$.status").value(412));
        updateReview(TestJwts.customer(author), review, "\"x\"", 3)
                .andExpect(status().isBadRequest());
        updateReview(TestJwts.customer(newUser()), review, "\"0\"", 3)
                .andExpect(status().isForbidden());
        updateReview(TestJwts.customer(author), UUID.randomUUID(), "\"0\"", 3)
                .andExpect(status().isNotFound());

        assertThat(rating(product)).containsEntry("rating_sum", 4).containsEntry("version", 1L);
    }

    // ---------- delete / hide ----------

    @Test
    void deleteIsSoftAndIdempotent() throws Exception {
        UUID product = activeProduct();
        String author = newUser();
        UUID review = reviewAs(author, product, 4);
        reviewAs(newUser(), product, 2);

        mvc.perform(delete(REVIEWS + "/" + review).with(TestJwts.customer(newUser())))
                .andExpect(status().isForbidden());

        mvc.perform(delete(REVIEWS + "/" + review).with(TestJwts.customer(author))).andExpect(status().isNoContent());
        mvc.perform(delete(REVIEWS + "/" + review).with(TestJwts.customer(author))).andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject("SELECT status FROM review WHERE id = ?", String.class, review))
                .isEqualTo("HIDDEN");
        assertThat(rating(product)).containsEntry("rating_count", 1).containsEntry("rating_sum", 2)
                .containsEntry("version", 3L);                                   // no double decrement
        assertThat(ratingEvents(product)).hasSize(3);

        mvc.perform(get(REVIEWS).param("productId", product.toString()))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating").value(2));
        updateReview(TestJwts.customer(author), review, "\"1\"", 5).andExpect(status().isNotFound());
        mvc.perform(delete(REVIEWS + "/" + UUID.randomUUID()).with(TestJwts.customer(author)))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminHideIsIdempotent() throws Exception {
        UUID product = activeProduct();
        UUID review = reviewAs(newUser(), product, 3);

        mvc.perform(post("/api/v1/admin/reviews/" + review + "/hide").with(TestJwts.admin()))
                .andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/admin/reviews/" + review + "/hide").with(TestJwts.admin()))
                .andExpect(status().isNoContent());
        assertThat(rating(product)).containsEntry("rating_count", 0).containsEntry("rating_sum", 0)
                .containsEntry("version", 2L);

        mvc.perform(post("/api/v1/admin/reviews/" + UUID.randomUUID() + "/hide").with(TestJwts.admin()))
                .andExpect(status().isNotFound());
    }

    // ---------- public list ----------

    @Test
    void publicListShowsPublishedNewestFirst() throws Exception {
        UUID product = activeProduct();
        UUID first = reviewAs(newUser(), product, 1);
        UUID second = reviewAs(newUser(), product, 2);
        UUID third = reviewAs(newUser(), product, 3);
        mvc.perform(post("/api/v1/admin/reviews/" + second + "/hide").with(TestJwts.admin()));

        mvc.perform(get(REVIEWS).param("productId", product.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(third.toString()))
                .andExpect(jsonPath("$.content[1].id").value(first.toString()))
                .andExpect(jsonPath("$.page.totalElements").value(2));

        mvc.perform(get(REVIEWS)).andExpect(status().isBadRequest());               // productId required
    }

    // ---------- aggregate + events ----------

    @Test
    void ratingAggregateAndEventsTrackEveryWrite() throws Exception {
        UUID product = activeProduct();
        String alice = newUser();
        UUID aliceReview = reviewAs(alice, product, 4);                                // 1 x 4      -> 4.00
        String bob = newUser();
        UUID bobReview = reviewAs(bob, product, 2);                                    // 4 + 2      -> 3.00
        updateReview(TestJwts.customer(alice), aliceReview, "\"0\"", 5)
                .andExpect(status().isOk());                                           // 5 + 2      -> 3.50
        mvc.perform(delete(REVIEWS + "/" + bobReview).with(TestJwts.customer(bob)))
                .andExpect(status().isNoContent());                                    // 5          -> 5.00

        assertThat(rating(product)).containsEntry("rating_count", 1).containsEntry("rating_sum", 5)
                .containsEntry("version", 4L);

        List<String> events = ratingEvents(product);
        assertThat(events).hasSize(4);
        List<String> expectedAverages = List.of("4.00", "3.00", "3.50", "5.00");
        List<Integer> expectedCounts = List.of(1, 2, 2, 1);
        for (int i = 0; i < 4; i++) {
            String e = events.get(i);
            assertThat((Object) JsonPath.read(e, "$.productId")).isEqualTo(product.toString());
            assertThat((Object) JsonPath.read(e, "$.ratingVersion")).isEqualTo(i + 1);
            assertThat((Object) JsonPath.read(e, "$.ratingCount")).isEqualTo(expectedCounts.get(i));
            // BigDecimal serialized as a JSON string, not a number
            assertThat((Object) JsonPath.read(e, "$.averageRating")).isEqualTo(expectedAverages.get(i));
        }

        // published to Kafka in order, keyed by product id
        List<ConsumerRecord<String, String>> records = readTopic(KafkaTopics.CATALOG_PRODUCT_RATINGS,
                r -> product.toString().equals(r.key()), 4, Duration.ofSeconds(30));
        assertThat(records).extracting(r -> (Integer) JsonPath.read(r.value(), "$.ratingVersion"))
                .containsExactly(1, 2, 3, 4);
        assertThat(records).extracting(r -> kafkaHeader(r, "eventType")).containsOnly("ProductRatingChanged");
    }
}
