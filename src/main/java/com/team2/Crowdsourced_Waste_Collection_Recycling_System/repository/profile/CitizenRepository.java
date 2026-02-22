package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.profile;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Citizen;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.User;
import jakarta.persistence.LockModeType;
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
}

