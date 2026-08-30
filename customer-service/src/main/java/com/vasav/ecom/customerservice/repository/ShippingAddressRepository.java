package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, UUID> {
}