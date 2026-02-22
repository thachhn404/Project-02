package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

// mapped from table waste_reports
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.enums.WasteReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WasteReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "citizen_id", nullable = false)
    private Citizen citizen;

    @Column(name = "description", length = 1000)
    @Nationalized
    private String description;

    @Column(name = "waste_type", nullable = false, length = 20)
    private String wasteType;

    @Column(name = "estimated_weight", precision = 10, scale = 2)
    private BigDecimal estimatedWeight; // Khối lượng ước tính (kg)

    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "address", length = 500)
    @Nationalized
    private String address;

    // @Column(name = "ward", length = 100)
    // private String ward;

    // @Column(name = "city", length = 100)
    // private String city;

    @Lob
    @Column(name = "images", columnDefinition = "NVARCHAR(MAX)")
    private String images;

    @Column(name = "cloudinary_public_id", length = 255)
    private String cloudinaryPublicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private WasteReportStatus status;

    // @Column(name = "is_valid")
    // private Boolean isValid;

    // @Column(name = "validation_note", length = 500)
    // private String validationNote;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "validated_by")
    // private User validatedBy;

    // @Column(name = "validated_at")
    // private LocalDateTime validatedAt;

    // @Column(name = "points_awarded")
    // private Integer pointsAwarded;

    // @Column(name = "quality_rating")
    // private Integer qualityRating;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
