package com.vasav.ecom.catalogservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogServiceApplicationTests extends AbstractIntegrationTest {

    @Autowired
    DataSource dataSource;

    @Test
    void contextLoadsAgainstTestcontainersNotTheRealDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            assertThat(url).doesNotContain(":5433").doesNotContain("catalog_db");
        }
        assertThat(jdbc.queryForObject("SELECT current_database()", String.class)).isNotEqualTo("catalog_db");
        assertThat(count("SELECT count(*) FROM flyway_schema_history WHERE success")).isEqualTo(1);
    }
}
