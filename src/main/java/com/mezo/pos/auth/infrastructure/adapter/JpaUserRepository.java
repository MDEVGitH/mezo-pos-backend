package com.mezo.pos.auth.infrastructure.adapter;

import com.mezo.pos.auth.domain.entity.User;
import com.mezo.pos.auth.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springRepo;

    @Override
    public User save(User user) {
        return springRepo.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springRepo.findById(id).filter(u -> !u.isDeleted());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springRepo.findByEmailValue(email.toLowerCase().trim());
    }

    @Override
    public boolean existsByEmail(String email) {
        return springRepo.existsByEmailValue(email.toLowerCase().trim());
    }
}
