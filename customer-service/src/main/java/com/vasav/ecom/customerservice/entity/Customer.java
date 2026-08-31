package com.vasav.ecom.customerservice.entity;

import com.vasav.ecom.customerservice.entity.enums.CommunicationPreference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column
    private String phone;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "additional_phones", columnDefinition = "text[]")
    private List<String> additionalPhones;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_communication_preference", nullable = false)
    private CommunicationPreference defaultCommunicationPreference;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShippingAddress> shippingAddresses = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
