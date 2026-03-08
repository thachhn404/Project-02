package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Integer> {
    Optional<Citizen> findByUser(User user);

    Optional<Citizen> findByUserId(Integer userId);

    Optional<Citizen> findByUser_Email(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Citizen c where c.id = :id")
    Optional<Citizen> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
            select c from Citizen c
            where (:region is null or lower(c.ward) = lower(:region) or lower(c.city) = lower(:region))
            order by coalesce(c.totalPoints, 0) desc, c.id asc
            """)
    Page<Citizen> findLeaderboard(@Param("region") String region, Pageable pageable);
}

