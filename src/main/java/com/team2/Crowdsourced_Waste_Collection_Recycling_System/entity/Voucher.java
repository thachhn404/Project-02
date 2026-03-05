package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "voucher_code", length = 10, unique = true)
    private String voucherCode;

    @Column(name = "banner_public_id", length = 255)
    private String bannerPublicId;

    @Column(name = "logo_public_id", length = 255)
    private String logoPublicId;

    @Column(name = "banner_url", length = 1000)
    private String bannerUrl;

    @Column(name = "logo_url", length = 1000)
    private String logoUrl;

    @Column(name = "title", nullable = false, length = 255)
    @Nationalized
    private String title;

    @Column(name = "value_display", length = 100)
    @Nationalized
    private String valueDisplay;

    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "remaining_stock")
    private Integer remainingStock;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "voucher_terms", joinColumns = @JoinColumn(name = "voucher_id"))
    @Column(name = "term", length = 500)
    @Nationalized
    private List<String> terms = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
