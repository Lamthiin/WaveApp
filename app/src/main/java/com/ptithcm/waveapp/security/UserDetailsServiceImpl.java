package com.ptithcm.waveapp.security;

import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserDetailsServiceImpl {
    private final UserRepository userRepository;

    public User loadUserByUsername(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
