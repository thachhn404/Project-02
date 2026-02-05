package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.authentication;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleCode(String roleCode);
    Optional<Role> findByRoleCodeIgnoreCase(String roleCode);
}
