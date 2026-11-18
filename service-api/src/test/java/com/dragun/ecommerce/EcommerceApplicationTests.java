package com.dragun.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("it")
@Import(TestcontainersConfiguration.class)
class EcommerceApplicationTests {

    @Test
    void contextLoads() {
        // Context starts on a real PostgreSQL: all Flyway migrations apply and Hibernate validates every entity
    }

}
