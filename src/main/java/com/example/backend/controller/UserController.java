package com.example.backend.controller;

import com.example.backend.dto.request.AccountInfoRequest;
import com.example.backend.entity.Account;
import com.example.backend.service.AccountService;
import com.example.backend.service.AmazonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private AmazonService amazonService;

    @GetMapping("/all")
    public String allAccess() {
        return "Public Content.";
    }

    @GetMapping("")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> userAccess(Principal principal) {
        if (!accountService.isEnabled(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is not enabled");
        }

        Account account = accountService.getAccount(principal.getName());
        return ResponseEntity.ok().body(account);
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateAccountInformation(Principal principal, @Valid @RequestBody AccountInfoRequest accountInfoRequest) {
        if (!accountService.isEnabled(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is not enabled");
        }

        String accountEmail = principal.getName();
        Account account = accountService.updateAccountInformation(accountEmail, accountInfoRequest);
        return ResponseEntity.ok().body(account);
    }

    @PostMapping(path = "/update/doc/1", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateDocumentFirstPage(Principal principal, @RequestParam("file") MultipartFile file) {
        if (!accountService.isEnabled(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is not enabled");
        }

        String accountEmail = principal.getName();

        try {
            amazonService.uploadFile(accountEmail, 1, file);
        } catch (java.io.IOException e) {
            System.out.println("Error during upload image");
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/update/doc/2", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateDocumentSecondPage(Principal principal, @RequestParam("file") MultipartFile file) {
        if (!accountService.isEnabled(principal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is not enabled");
        }

        String accountEmail = principal.getName();

        try {
            amazonService.uploadFile(accountEmail, 2, file);
        } catch (java.io.IOException e) {
            System.out.println("Error during upload image");
        }

        return ResponseEntity.ok().build();
    }
}
