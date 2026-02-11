package com.tpms.repository;

import com.tpms.entity.TrainingEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface TrainingEnrollmentRepository extends JpaRepository<TrainingEnrollment, Long> {
    List<TrainingEnrollment> findByStudentId(Long studentId);
    List<TrainingEnrollment> findByTrainingId(Long trainingId);
    Optional<TrainingEnrollment> findByTrainingIdAndStudentId(Long trainingId, Long studentId);
    
    @Modifying
    @Transactional
    void deleteByTrainingId(Long trainingId);
}
