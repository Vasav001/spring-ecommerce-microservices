package com.vasav.ecom.customerservice.mapper;

import com.vasav.ecom.customerservice.dto.CreateShippingAddressRequest;
import com.vasav.ecom.customerservice.dto.ShippingAddressDto;
import com.vasav.ecom.customerservice.dto.UpdateShippingAddressRequest;
import com.vasav.ecom.customerservice.entity.ShippingAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShippingAddressMapper {
    ShippingAddressDto toDto(ShippingAddress entity);
    List<ShippingAddressDto> toDtoList(List<ShippingAddress> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShippingAddress toEntity(CreateShippingAddressRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateShippingAddressRequest request, @MappingTarget ShippingAddress entity);
}
