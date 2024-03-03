package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email"),
                @UniqueConstraint(columnNames = "phone")
        })
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String phone;

    private String username;

    private String password;

    private String firstName;

    private String lastName;

    private String fatherName;

    private String address;

    private String zipCode;

    private String city;

    private String country;

    private String linkToFirstPassportPage;

    private String linkToSecondPassportPage;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "account_wallet_id", referencedColumnName = "id")
    private AccountWallet accountWallet = new AccountWallet();

    @Column(name = "is_enabled")
    private boolean isEnabled;

    private boolean isNonLocked;

    private int failedLoginAttempts;

    private LocalDateTime lastLoginAttempt;
//
//    public boolean isAccountLocked() {
//        // Implement logic to check if the account is locked
//        final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
//        final Duration LOCKOUT_DURATION = Duration.ofHours(2);
//
//        if (failedLoginAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
//            // Check if the last login attempt was within the lockout duration
//            LocalDateTime now = LocalDateTime.now();
//            LocalDateTime lockoutEndTime = lastLoginAttempt.plus(LOCKOUT_DURATION);
//            return now.isBefore(lockoutEndTime);
//        }
//        return false;
//    }

     // TODO: add verifications documents
   // private int failedDocumentsVerificationAttempts;

    public Account(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
