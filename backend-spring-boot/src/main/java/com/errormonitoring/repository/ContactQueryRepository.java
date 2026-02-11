package com.errormonitoring.repository;

import com.errormonitoring.model.ContactQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactQueryRepository extends JpaRepository<ContactQuery, Integer> {
    List<ContactQuery> findByStatus(String status);
    Page<ContactQuery> findByStatus(String status, Pageable pageable);
    List<ContactQuery> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}

