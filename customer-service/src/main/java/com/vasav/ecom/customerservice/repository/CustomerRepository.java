package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.Customer;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    @Modifying
    @Query(value = "INSERT INTO customer (id) VALUES (:id) ON CONFLICT (id) DO NOTHING", nativeQuery = true)
    int createIfAbsent(@Param("id") String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> lockById(@Param("id") String id);
}
