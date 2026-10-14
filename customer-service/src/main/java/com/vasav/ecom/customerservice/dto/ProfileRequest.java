package com.vasav.ecom.customerservice.dto;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record ProfileRequest(
        @URL @Size(max = 1000) String profilePictureUrl
) {
}
