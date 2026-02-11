package com.errormonitoring.repository;

import com.errormonitoring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByEmailContainingIgnoreCase(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByIsActiveTrue();
}

