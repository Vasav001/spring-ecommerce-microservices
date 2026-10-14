package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCustomerIdOrderByCreatedAt(String customerId);

    Optional<Address> findByIdAndCustomerId(UUID id, String customerId);

    long countByCustomerId(String customerId);
}
