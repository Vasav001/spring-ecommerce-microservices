package com.vasav.ecom.customerservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Requires running Postgres (customer_db) and Keycloak; run manually with the dev profile")
@SpringBootTest
class CustomerServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
