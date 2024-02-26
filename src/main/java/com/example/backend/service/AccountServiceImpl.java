package com.example.backend.service;

import com.example.backend.config.jwt.JwtUtils;
import com.example.backend.dto.request.AccountInfoRequest;
import com.example.backend.dto.request.FindAccountDtoRequest;
import com.example.backend.dto.request.IdTokenRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.entity.Account;
import com.example.backend.entity.AccountWallet;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.error.AccountAlreadyExistsException;
import com.example.backend.error.AccountNotFoundException;
import com.example.backend.repo.AccountWalletRepository;
import com.example.backend.repo.RoleRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.interfaces.AccountService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class AccountServiceImpl implements AccountService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    AccountWalletRepository walletRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    PasswordEncoder encoder;
    private final GoogleIdTokenVerifier verifier;

    public AccountServiceImpl(@Value("${app.googleClientId}") String clientId, UserRepository userRepository,
                              JwtUtils jwtUtils, RoleRepository roleRepository, PasswordEncoder encoder, AccountWalletRepository accountWalletRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.walletRepository = accountWalletRepository;
        this.jwtUtils = jwtUtils;
        this.encoder = encoder;
        NetHttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    public String registerUser(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new AccountAlreadyExistsException("Error: User already exists");
        }

        Account account = new Account();
        Set<Role> roles = new HashSet<>();

        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        AccountWallet unusedWallet = walletRepository.findAnyUnusedWallet()
                .orElseThrow(() -> new RuntimeException("Error: No unused wallet found in the database."));
        unusedWallet.setUsed(true);
        unusedWallet.setAccountEmail(signupRequest.getEmail());
        walletRepository.updateAccountEmailById(unusedWallet.getId(), signupRequest.getEmail());

        account.setAccountWallet(unusedWallet);
        account.setEmail(signupRequest.getEmail());
        account.setPhone(signupRequest.getPhone());
        account.setPassword(encoder.encode(signupRequest.getPassword()));

        account.setRoles(roles);

        Account createdAccount = userRepository.save(account);
        return jwtUtils.createToken(createdAccount, false);
    }

    public String loginUser(Authentication authentication) {
        return jwtUtils.generateJwtToken(authentication, false);
    }

    public Account getAccount(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AccountNotFoundException("Error: User does not exists by provided email in the database."));
    }

    public Account getAccountByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseThrow(() ->
                new AccountNotFoundException("Error: User does not exists by provided phone in the database."));
    }

    public Account getAccountForLogin(FindAccountDtoRequest findAccountDtoRequest) {
        String email = findAccountDtoRequest.getAccountEmail();
        String phone = findAccountDtoRequest.getAccountPhone();

        if (email != null) {
            return getAccount(email);
        } else {
            return getAccountByPhone(phone);
        }
    }

    @Transactional
    public String loginOAuthGoogle(IdTokenRequest requestBody) {
        Account account = verifyIDToken(requestBody.getIdToken());
        if (account == null) {
            throw new AccountNotFoundException("Error: User can not be logged using Google account");
        }
        account = createOrUpdateUser(account);
        return jwtUtils.createToken(account, false);
    }

    @Transactional
    public Account createOrUpdateUser(Account account) {
        Account existingAccount = userRepository.findByEmail(account.getEmail()).orElse(null);
        if (existingAccount == null) {
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            AccountWallet unusedWallet = walletRepository.findAnyUnusedWallet()
                    .orElseThrow(() -> new RuntimeException("Error: No unused wallet found in the database."));
            unusedWallet.setUsed(true);
            unusedWallet.setAccountEmail(account.getEmail());
            walletRepository.updateAccountEmailById(unusedWallet.getId(), account.getEmail());

            account.setAccountWallet(unusedWallet);
            account.setRoles(roles);
            userRepository.save(account);
            return account;
        }
        return existingAccount;
    }

    @Transactional
    public Account updateAccountInformation(String accountEmail, AccountInfoRequest accountInfoRequest) {
        Account existingAccount = getAccount(accountEmail);
        if (existingAccount != null) {
            existingAccount.setFirstName(accountInfoRequest.getFirstName());
            existingAccount.setLastName(accountInfoRequest.getLastName());
            existingAccount.setFatherName(accountInfoRequest.getFatherName());
            existingAccount.setAddress(accountInfoRequest.getAddress());
            existingAccount.setZipCode(accountInfoRequest.getZipCode());
            existingAccount.setCity(accountInfoRequest.getCity());
            existingAccount.setCountry(accountInfoRequest.getCountry());
        }

        assert existingAccount != null;
        userRepository.save(existingAccount);
        return existingAccount;
    }

    public void updateDocumentLink(String email, String link, int page) {
        Account account = getAccount(email);

        if (page == 1) {
            account.setLinkToFirstPassportPage(link);
        } else if (page == 2) {
            account.setLinkToSecondPassportPage(link);
        }

        userRepository.save(account);
    }

    public void activateUserAccount(String email) {
        Account account = getAccount(email);
        account.setEnabled(true);

        userRepository.save(account);
    }


    private Account verifyIDToken(String idToken) {
        try {
            GoogleIdToken idTokenObj = verifier.verify(idToken);
            if (idTokenObj == null) {
                return null;
            }
            GoogleIdToken.Payload payload = idTokenObj.getPayload();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");
            String email = payload.getEmail();
            String pictureUrl = (String) payload.get("picture");

            return new Account(firstName, lastName, email);
        } catch (GeneralSecurityException | IOException e) {
            return null;
        }
    }

    public boolean isEnabled(Principal principal) {
        String accountEmail = principal.getName();
        Account account = getAccount(accountEmail);
        return account.isEnabled();
    }
}
