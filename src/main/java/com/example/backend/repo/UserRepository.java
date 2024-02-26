package com.example.backend.repo;

import com.example.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhone(String phone);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
