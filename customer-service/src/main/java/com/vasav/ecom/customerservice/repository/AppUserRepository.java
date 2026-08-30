package com.vasav.ecom.customerservice.repository;

import com.vasav.ecom.customerservice.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
}