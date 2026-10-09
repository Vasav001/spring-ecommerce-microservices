package com.vasav.ecom.customerservice.service;

import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.security.CurrentUser;

public interface AppUserService {
    AppUser syncWithKeycloak(CurrentUser currentUser);
}
