package com.ptithcm.waveapp.repository.impl;

import com.ptithcm.waveapp.model.User;
import com.ptithcm.waveapp.repository.UserRepository;
import java.util.*;

public class MockUserRepository implements UserRepository {
    private static final Map<String, User> users = new HashMap<>();

    static {
        User admin = User.builder()
                .id("user-1")
                .username("admin")
                .email("admin@wave.com")
                .password("admin123")
                .name("Wave Admin")
                .role(User.Role.ADMIN)
                .verified(true)
                .active(true)
                .build();
        users.put(admin.getId(), admin);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return users.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return users.values().stream().filter(u -> phone.equals(u.getPhone())).findFirst();
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return users.values().stream().filter(u -> googleId.equals(u.getGoogleId())).findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.values().stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return users.values().stream().anyMatch(u -> phone.equals(u.getPhone()));
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }
        users.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }
}
