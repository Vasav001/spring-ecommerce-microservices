package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {
    List<ShippingAddress> findByCustomerId(UUID customerId);

    Optional<ShippingAddress> findByIdAndCustomerId(UUID id, UUID customerId);

    long countByCustomerId(UUID customerId);
}
