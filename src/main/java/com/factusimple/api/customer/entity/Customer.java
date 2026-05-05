package com.factusimple.api.customer.entity;

import com.factusimple.api.establishments.entity.Establishment;
import com.factusimple.api.infrastructure.persistence.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "customers",
        indexes = {
                @Index(name = "idx_customer_establishment_id", columnList = "establishment_id"),
                @Index(name = "idx_customer_identification", columnList = "identification")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uk_customer_identification_per_establishment",
                columnNames = {"establishment_id", "identification"}
        )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establishment_id", nullable = false)
    private Establishment establishment;

    @NotBlank(message = "El tipo de identificación es requerido")
    @Column(name = "id_type_code", nullable = false, length = 4)
    private String idTypeCode;          // "31" = NIT, "13" = CC, etc.

    @NotBlank(message = "La identificación es requerida")
    @Column(nullable = false, length = 30)
    private String identification;

    @Column(length = 2)
    private String dv;

    @Column(name = "legal_org_code", length = 10)
    private String legalOrgCode;        // "1" = Persona Jurídica, "2" = Persona Natural

    @Column(length = 255)
    private String company;

    @Column(length = 255)
    private String names;

    @Column(name = "trade_name", length = 255)
    private String tradeName;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "municipality_code", length = 10)
    private String municipalityCode;

    @Column(name = "tribute_code", length = 4)
    private String tributeCode;

    @Column(name = "fiscal_responsibility", length = 50)
    private String fiscalResponsibility;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
