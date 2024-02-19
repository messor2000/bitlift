package com.example.backend.controller;

import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Account> userAccess(Principal principal) {
        Account account = accountService.getAccount(principal.getName());
        return ResponseEntity.ok().body(account);
    }
}
