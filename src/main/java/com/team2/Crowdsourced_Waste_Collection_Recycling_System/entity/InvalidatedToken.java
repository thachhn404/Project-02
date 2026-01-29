package com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "invalidated_tokens")
/**
 * Bảng lưu token đã bị thu hồi (denylist).
 *
 * - id: JWT ID (jti) của token
 * - expiryTime: thời điểm hết hạn để có thể dọn dữ liệu theo TTL
 *
 * Token sẽ bị coi là không hợp lệ nếu jti tồn tại trong bảng này.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvalidatedToken {
    @Id
    private String id;
    private Date expiryTime;
}
