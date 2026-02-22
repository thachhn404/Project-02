package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table roles
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.Set;
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "role_code", nullable = false, unique = true, length = 20)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 50)
    @Nationalized
    private String roleName;

    @Column(name = "description", length = 500)
    @Nationalized
    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<RolePermission> rolePermissions;
}
