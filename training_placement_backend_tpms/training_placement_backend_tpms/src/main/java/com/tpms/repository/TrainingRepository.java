package com.tpms.repository;

import com.tpms.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByTrainerId(Long trainerId);
}
