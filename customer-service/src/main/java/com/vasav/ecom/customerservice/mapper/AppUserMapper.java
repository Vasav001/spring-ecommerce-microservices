package com.vasav.ecom.customerservice.mapper;

import com.vasav.ecom.customerservice.dto.AppUserDto;
import com.vasav.ecom.customerservice.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppUserMapper {
    AppUserDto toDto(AppUser entity);
    AppUser toEntity(AppUserDto dto);
}
