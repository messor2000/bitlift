package com.example.backend.service;

import com.example.backend.dto.payload.request.NewWalletRequest;
import com.example.backend.entity.AccountWallet;
import com.example.backend.repo.AccountWalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletService {
    private final AccountWalletRepository walletRepository;

    public WalletService(AccountWalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public AccountWallet createWallet(NewWalletRequest walletRequest) {
        return walletRepository.save(new AccountWallet(walletRequest.getWallet()));
    }

    public List<AccountWallet> wallets() {
        return walletRepository.findAll();
    }
}
