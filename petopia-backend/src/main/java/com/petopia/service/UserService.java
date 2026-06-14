package com.petopia.service;

import com.petopia.dto.LoginRequest;
import com.petopia.dto.LoginResponse;
import com.petopia.dto.RegisterRequest;
import com.petopia.model.User;
import com.petopia.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private final UserRepository userRepository;

    // Simple in-memory token store: token → userId
    // In production this would be JWT or a Redis-backed session store
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── Register ─────────────────────────────────────────────
    @Transactional
    public User register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists.");
        }
        String hash = BCrypt.hashpw(req.getPassword(), BCrypt.gensalt(12));
        User user = new User(req.getUsername(), hash,
                req.getDisplayName() == null ? req.getUsername() : req.getDisplayName());
        return userRepository.save(user);
    }

    // ── Login ────────────────────────────────────────────────
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!BCrypt.checkpw(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Issue a simple UUID token
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());

        return new LoginResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                token
        );
    }

    // ── Logout ───────────────────────────────────────────────
    public void logout(String token) {
        tokenStore.remove(token);
    }

    // ── Token validation ─────────────────────────────────────
    public Optional<User> getUserByToken(String token) {
        Long userId = tokenStore.get(token);
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    public boolean isValidToken(String token) {
        return tokenStore.containsKey(token);
    }
}
