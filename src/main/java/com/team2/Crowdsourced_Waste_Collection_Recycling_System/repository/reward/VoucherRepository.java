package com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.reward;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    List<Voucher> findAllByActiveTrueOrderByIdDesc();

    Optional<Voucher> findByTitleIgnoreCase(String title);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Voucher v where v.id = :id")
    Optional<Voucher> findByIdForUpdate(@Param("id") Integer id);
}
