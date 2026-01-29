package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
/**
 * Repository truy vấn bảng invalidated_tokens (denylist token).
 *
 * Dùng chủ yếu để:
 * - kiểm tra jti đã bị thu hồi hay chưa (existsById)
 * - lưu bản ghi thu hồi khi logout/refresh
 */
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
