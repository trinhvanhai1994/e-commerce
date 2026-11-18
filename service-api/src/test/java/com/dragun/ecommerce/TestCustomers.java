package com.dragun.ecommerce;

import com.dragun.ecommerce.model.entity.Customer;
import com.dragun.ecommerce.repository.CustomerRepository;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

/** Customer rows for tests that need a logged-in account. Phones are random so tests never collide. */
public final class TestCustomers {

    private TestCustomers() {
    }

    public static String randomPhone() {
        return "09" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
    }

    public static Customer create(CustomerRepository repository, String fullName) {
        return repository.saveAndFlush(Customer.builder()
            .phone(randomPhone())
            .passwordHash("not-a-real-hash")
            .fullName(fullName)
            .status(Customer.STATUS_ACTIVE)
            .consentAt(OffsetDateTime.now())
            .consentVersion("test")
            .build());
    }
}
