package com.valenciabank.api.service;

import com.valenciabank.api.dto.UserSummaryResponse;
import com.valenciabank.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt()))
                .toList();
    }
}
