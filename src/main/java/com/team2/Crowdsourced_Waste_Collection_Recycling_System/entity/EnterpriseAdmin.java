package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table enterprise_admins
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise_admins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnterpriseAdmin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enterprise_id", nullable = false)
    private RecyclingEnterprise enterprise;

    @Column(name = "position", length = 100)
    private String position;

    @Column(name = "is_owner")
    private Boolean isOwner;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
