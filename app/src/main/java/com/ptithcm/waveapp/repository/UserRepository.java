package com.ptithcm.waveapp.repository;

import com.ptithcm.waveapp.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByPhone(String phone);
    Optional<User> findByGoogleId(String googleId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByPhone(String phone);
    void save(User user);
    Optional<User> findById(String id);

    default Optional<User> findByIdentifier(String id) {
        Optional<User> u = findByEmail(id);
        if (u.isPresent()) return u;
        u = findByPhone(id);
        if (u.isPresent()) return u;
        return findByUsername(id);
    }
}
