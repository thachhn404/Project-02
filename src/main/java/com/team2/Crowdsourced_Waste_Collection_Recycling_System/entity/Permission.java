package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table permissions
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "permission_code", nullable = false, unique = true, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 255)
    @Nationalized
    private String permissionName;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "description", length = 500)
    @Nationalized
    private String description;
}
