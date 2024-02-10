package com.example.backend.repo;

import com.example.backend.entity.AccountWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface AccountWalletRepository extends JpaRepository<AccountWallet, Long> {
    @Query("SELECT w FROM AccountWallet w WHERE w.used = false AND w.accountEmail IS NULL")
    Optional<AccountWallet> findAnyUnusedWallet();

    @Transactional
    @Modifying
    @Query("UPDATE AccountWallet w SET w.accountEmail = :accountEmail WHERE w.id = :walletId")
    void updateAccountEmailById(Long walletId, String accountEmail);
}
