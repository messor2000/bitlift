package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

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

    public Account(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
