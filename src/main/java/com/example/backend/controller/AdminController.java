package com.example.backend.controller;

import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.NewWalletRequest;
import com.example.backend.entity.AccountWallet;
import com.example.backend.service.WalletService;
import com.example.backend.service.interfaces.AccountService;
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
    @Autowired
    private AccountService accountService;

    @GetMapping("/admin")
    public String adminAccess() {
        return "Admin Board.";
    }

    // TODO; get all account by admin
//    @GetMapping("/users")
//    public ResponseEntity<List<AccountDto>> getAccounts() {
//        List<AccountDto> allAccounts = accountService.getAllAccount();
//
//        return allAccounts;
//    }

    // TODO; approve/not approve account by admin
//    @PostMapping("/approve")
//    public ResponseEntity<List<AccountDto>> getAccounts() {
//        List<AccountDto> allAccounts = accountService.getAllAccount();
//
//        return allAccounts;
//    }

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
