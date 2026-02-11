package com.errormonitoring.service;

import com.errormonitoring.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    public long getActiveUsersCount() {
        try {
            return userRepository.countByIsActiveTrue();
        } catch (Exception e) {
            return 0;
        }
    }
}