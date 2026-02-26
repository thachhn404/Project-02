package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Admin: lấy all users, lọc theo status
    List<User> findAllByStatusOrderByCreatedAtDesc(String status);

    // Admin: lọc theo roleCode
    List<User> findAllByRole_RoleCodeOrderByCreatedAtDesc(String roleCode);

    // Admin: lọc theo cả status và roleCode
    List<User> findAllByStatusAndRole_RoleCodeOrderByCreatedAtDesc(String status, String roleCode);
}
