package com.example.backend.controller;

import com.example.backend.dto.request.NewWalletRequest;
import com.example.backend.entity.AccountWallet;
import com.example.backend.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private WalletService walletService;

    @GetMapping("/admin")
    public String adminAccess() {
        return "Admin Board.";
    }

    @PostMapping("/wallet")
    public ResponseEntity<AccountWallet> addNewWallet(@RequestBody NewWalletRequest walletRequest) {
        AccountWallet accountWallet = walletService.createWallet(walletRequest);

        return ResponseEntity.ok(accountWallet);
    }

    @GetMapping("/wallets")
    public List<AccountWallet> getAllWallets() {
        return walletService.wallets();
    }
}
