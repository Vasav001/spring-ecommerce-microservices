package com.vasav.ecom.customerservice.service.impl;

import com.vasav.ecom.customerservice.dto.CustomerResponseDto;
import com.vasav.ecom.customerservice.dto.UpdateCustomerProfileRequest;
import com.vasav.ecom.customerservice.entity.AppUser;
import com.vasav.ecom.customerservice.entity.Customer;
import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;
import com.vasav.ecom.customerservice.exception.BusinessRuleException;
import com.vasav.ecom.customerservice.exception.InvalidRequestException;
import com.vasav.ecom.customerservice.exception.ResourceNotFoundException;
import com.vasav.ecom.customerservice.exception.UnauthorizedAccessException;
import com.vasav.ecom.customerservice.mapper.CustomerMapper;
import com.vasav.ecom.customerservice.repository.CustomerRepository;
import com.vasav.ecom.customerservice.security.CurrentUser;
import com.vasav.ecom.customerservice.service.AppUserService;
import com.vasav.ecom.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "phone", "active", "createdAt", "updatedAt");

    private final CustomerRepository customerRepository;
    private final AppUserService appUserService;
    private final CustomerMapper customerMapper;
    private final TransactionTemplate transactionTemplate;

    @Override
    public CustomerResponseDto getOrCreateCurrentCustomer(CurrentUser currentUser) {
        return withProvisioningRetry(() -> transactionTemplate.execute(status ->
                customerMapper.toDto(provision(currentUser))));
    }

    @Override
    public Customer getOrCreateCustomerEntity(CurrentUser currentUser) {
        return withProvisioningRetry(() -> transactionTemplate.execute(status -> provision(currentUser)));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(UUID customerId) {
        Customer customer = customerRepository.findByIdWithUser(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerResponseDto updateCustomerProfile(CurrentUser currentUser, UpdateCustomerProfileRequest request) {
        Customer customer = customerRepository.findByUserKeycloakUserId(currentUser.keycloakUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
        assertActive(customer);

        if (request.defaultCommunicationPreference() == CommunicationPreference.SMS
                && (request.phone() == null || request.phone().isBlank())) {
            throw new BusinessRuleException("A phone number is required when SMS is the communication preference");
        }

        customerMapper.updateEntityFromRequest(request, customer);
        return customerMapper.toDto(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponseDto> getAllCustomers(Pageable pageable) {
        pageable.getSort().forEach(order -> {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new InvalidRequestException("Cannot sort by: " + order.getProperty());
            }
        });
        return customerRepository.findAllWithUser(pageable).map(customerMapper::toDto);
    }

    @Override
    @Transactional
    public void setCustomerActiveStatus(UUID customerId, boolean active) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));
        customer.setActive(active);
        customerRepository.save(customer);
    }

    private Customer provision(CurrentUser currentUser) {
        AppUser appUser = appUserService.syncWithKeycloak(currentUser);
        Customer customer = customerRepository.findByUserKeycloakUserId(appUser.getKeycloakUserId())
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setUser(appUser);
                    newCustomer.setDefaultCommunicationPreference(CommunicationPreference.EMAIL);
                    newCustomer.setActive(true);
                    return customerRepository.save(newCustomer);
                });
        assertActive(customer);
        return customer;
    }


    private <T> T withProvisioningRetry(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataIntegrityViolationException e) {
            return action.get();
        }
    }

    private static void assertActive(Customer customer) {
        if (!customer.isActive()) {
            throw new UnauthorizedAccessException("Customer account is deactivated");
        }
    }
}
