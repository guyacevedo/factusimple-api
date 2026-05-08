package com.factusimple.api.user.entity;

import com.factusimple.api.infrastructure.persistence.BaseEntity;
import com.factusimple.api.plan.entity.Plan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_role_id", columnList = "role_id"),
        @Index(name = "idx_user_plan_id", columnList = "plan_id"),
        @Index(name = "idx_user_is_active", columnList = "is_active")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Column(nullable = false, length = 255)
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Column(nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "El apellido es requerido")
    @Column(nullable = false, length = 100)
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Teléfono inválido")
    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Builder.Default
    @Column(name = "product_count", nullable = false)
    private Integer productsCount = 0;

    @Builder.Default
    @Column(name = "customer_count", nullable = false)
    private Integer customersCount = 0;

    @Builder.Default
    @Column(name = "invoice_count", nullable = false)
    private Integer invoiceCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
