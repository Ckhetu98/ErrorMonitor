package com.errormonitoring.repository;

import com.errormonitoring.model.UserOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserOTPRepository extends JpaRepository<UserOTP, Integer> {
    Optional<UserOTP> findByUserIdOrderByCreatedAtDesc(Integer userId);
    
    @Modifying
    @Query("DELETE FROM UserOTP u WHERE u.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}

