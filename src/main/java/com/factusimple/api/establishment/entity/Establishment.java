package com.factusimple.api.establishment.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import com.factusimple.api.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "establishments", indexes = {
        @Index(name = "idx_establishment_user_id", columnList = "user_id"),
        @Index(name = "idx_establishment_nit", columnList = "nit")
})
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Establishment extends BaseEntity {

    @NotBlank(message = "La razón social es requerida")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 100)
    private String email;

    @Column(name = "municipality_code", length = 5)
    private String municipalityCode;

    @NotBlank(message = "El NIT es requerido")
    @Column(nullable = false, length = 20)
    private String nit;

    @Column(length = 2)
    private String dv;

    @Column(name = "legal_org_code", length = 10)
    private String legalOrgCode;

    @Column(name = "tribute_code", length = 4)
    private String tributeCode;

    @Column(name = "fiscal_responsibility", length = 50)
    private String fiscalResponsibility;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}