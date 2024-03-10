package com.example.backend.controller;

import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.ChangeAccountVerificationStatusDtoRequest;
import com.example.backend.dto.request.NewWalletRequest;
import com.example.backend.entity.AccountWallet;
import com.example.backend.service.WalletService;
import com.example.backend.service.interfaces.AccountService;
import com.example.backend.util.SimplePage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private WalletService walletService;
    @Autowired
    private AccountService accountService;

    @GetMapping("/")
    public String adminAccess() {
        return "Admin Board.";
    }

    @GetMapping("/users")
//    public ResponseEntity<SimplePage<AccountDto>> getAccounts(@SortDefault(sort = "priRole") @PageableDefault(size = 20) final Pageable pageable) {
    public ResponseEntity<SimplePage<AccountDto>> getAccounts(@PageableDefault(size = 8, page = 0) final Pageable pageable) {
        return ResponseEntity.ok(accountService.findAll(pageable));
    }

    @PostMapping("/change/user/verification")
    public ResponseEntity<List<AccountDto>> approveUserAccount(@Valid @RequestBody ChangeAccountVerificationStatusDtoRequest changeAccountVerificationStatusDtoRequest) {
        accountService.changeAccountVerificationStatus(changeAccountVerificationStatusDtoRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/wallet")
    public ResponseEntity<AccountWallet> addNewWallet(@RequestBody NewWalletRequest walletRequest) {
        AccountWallet accountWallet = walletService.createWallet(walletRequest);

        return ResponseEntity.ok(accountWallet);
    }

    @PostMapping("/verify/user")
    public ResponseEntity<AccountWallet> verifyUserAccount(@RequestBody NewWalletRequest walletRequest) {
        AccountWallet accountWallet = walletService.createWallet(walletRequest);

        return ResponseEntity.ok(accountWallet);
    }

    @PostMapping("/non-verify/user")
    public ResponseEntity<AccountWallet> nonVerifyUserAccount(@RequestBody NewWalletRequest walletRequest) {
        AccountWallet accountWallet = walletService.createWallet(walletRequest);

        return ResponseEntity.ok(accountWallet);
    }

    @GetMapping("/wallets")
    public List<AccountWallet> getAllWallets() {
        return walletService.wallets();
    }
}
