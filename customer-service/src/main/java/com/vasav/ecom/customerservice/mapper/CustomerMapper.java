package com.vasav.ecom.customerservice.mapper;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {AppUserMapper.class, ShippingAddressMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {
    CustomerResponseDto toDto(Customer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "shippingAddresses", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCustomerProfileRequest request, @MappingTarget Customer entity);
}
