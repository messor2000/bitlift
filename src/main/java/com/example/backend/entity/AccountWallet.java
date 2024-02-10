package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_wallet",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"wallet", "accountEmail"})
        }
)
public class AccountWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String accountEmail;

    @Column(unique = true)
    private String wallet;

    private boolean used;

    public AccountWallet(String wallet) {
        this.wallet = wallet;
    }
}
