package com.example.backend.service;

import com.example.backend.config.jwt.JwtUtils;
import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.*;
import com.example.backend.entity.Account;
import com.example.backend.entity.AccountWallet;
import com.example.backend.entity.ERole;
import com.example.backend.entity.Role;
import com.example.backend.error.AccountAlreadyExistsException;
import com.example.backend.error.AccountNotFoundException;
import com.example.backend.error.OldPasswordMissmatchException;
import com.example.backend.error.PasswordMissmatchException;
import com.example.backend.repo.AccountWalletRepository;
import com.example.backend.repo.RoleRepository;
import com.example.backend.repo.UserRepository;
import com.example.backend.service.interfaces.AccountService;
import com.example.backend.util.SimplePage;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        account.setNonLocked(true);

        account.setRoles(roles);

        Account createdAccount = userRepository.save(account);
        return jwtUtils.createToken(createdAccount, false);
    }

    public String loginUser(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Account account = getAccount(userPrincipal.getEmail());
        account.setLastLoginAttempt(LocalDateTime.now());

        userRepository.save(account);

        return jwtUtils.generateJwtToken(authentication, false);
    }

    public AccountDto getAccountDto(String email) {
        Account account = userRepository.findByEmail(email).orElseThrow(() ->
                new AccountNotFoundException("Error: User does not exists by provided email in the database."));

        return new AccountDto(account.getEmail(), account.getPhone(), account.getUsername(), account.getFatherName(), account.getLastName(),
                account.getFatherName(), account.getAddress(), account.getZipCode(), account.getCity(),
                account.getCountry(), account.getDocumentCountry());
    }

    public Account getAccount(String email) {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new AccountNotFoundException("Error: User does not exists by provided email in the database."));
    }

    public Account getAccountByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseThrow(() ->
                new AccountNotFoundException("Error: User does not exists by provided phone in the database."));
    }

    public AccountDto getAccountByEmailOrPhone(FindAccountDtoRequest findAccountDtoRequest) {
        String email = findAccountDtoRequest.getAccountEmail();
        String phone = findAccountDtoRequest.getAccountPhone();

        if (email != null) {
            return mapToDTO(getAccount(email));
        } else if (phone != null) {
            return mapToDTO(getAccountByPhone(phone));
        } else {
            return null;
        }
    }

    @Transactional
    public String loginOAuthGoogle(IdTokenRequest requestBody) {
        Account account = verifyIDToken(requestBody.getIdToken());
        if (account == null) {
            throw new AccountNotFoundException("Error: User can not be logged using Google account");
        }
        account = createOrUpdateUser(account);
        account.setLastLoginAttempt(LocalDateTime.now());
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
            account.setNonLocked(true);
            account.setRoles(roles);
            userRepository.save(account);
            return account;
        }
        return existingAccount;
    }

    @Transactional
    public AccountDto updateAccountInformation(String accountEmail, AccountInfoRequest accountInfoRequest) {
        Account existingAccount = getAccount(accountEmail);
        if (existingAccount != null) {
            existingAccount.setFirstName(accountInfoRequest.getFirstName());
            existingAccount.setLastName(accountInfoRequest.getLastName());
            existingAccount.setFatherName(accountInfoRequest.getFatherName());
            existingAccount.setAddress(accountInfoRequest.getAddress());
            existingAccount.setZipCode(accountInfoRequest.getZipCode());
            existingAccount.setCity(accountInfoRequest.getCity());
            existingAccount.setCountry(accountInfoRequest.getCountry());
            existingAccount.setDocumentCountry(accountInfoRequest.getDocumentCountry());
        }

        assert existingAccount != null;
        Account updatedAccount = userRepository.save(existingAccount);

        return mapToDTO(updatedAccount);
    }

    public SimplePage<AccountDto> findAll(final Pageable pageable) {
        Page<Account> page = userRepository.findAll(pageable);
        List<AccountDto> accountDtos = page.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return new SimplePage<>(accountDtos, pageable, page.getTotalElements());
    }

    public AccountDto createNewPassword(CreateNewPasswordDtoRequest createNewPasswordDtoRequest, AccountDto accountDto) {
        Account account = getAccount(accountDto.getEmail());

        if (createNewPasswordDtoRequest.getNewPassword().equals(createNewPasswordDtoRequest.getConfirmNewPassword())) {
            account.setPassword(encoder.encode(createNewPasswordDtoRequest.getNewPassword()));
        } else {
            throw new PasswordMissmatchException("Password mismatch");
        }

        Account updatedAccount = userRepository.save(account);

        return mapToDTO(updatedAccount);
    }

    public AccountDto changePassword(ChangePasswordDtoRequest changePasswordDtoRequest, Principal principal) {
        Account account = getAccount(principal.getName());

        boolean checkPassword = isTruePassword(account, changePasswordDtoRequest.getOldPassword());
        if (checkPassword) {
            if (changePasswordDtoRequest.getNewPassword().equals(changePasswordDtoRequest.getConfirmNewPassword())) {
                account.setPassword(encoder.encode(changePasswordDtoRequest.getNewPassword()));
            } else {
                throw new PasswordMissmatchException("New password mismatch");
            }
        } else {
            throw new OldPasswordMissmatchException("Old password is incorrect");
        }

        Account updatedAccount = userRepository.save(account);

        return mapToDTO(updatedAccount);
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

    public void lockedUserAccount(String email) {
        Account account = getAccount(email);
        if (account.getFailedLoginAttempts() == 4) {
            account.setNonLocked(false);
        } else {
            int failedLoginAttempts = account.getFailedLoginAttempts();
            account.setFailedLoginAttempts(++failedLoginAttempts);
        }

        userRepository.save(account);
    }

    public void changeAccountVerificationStatus(ChangeAccountVerificationStatusDtoRequest changeAccountVerificationStatusDtoRequest) {
        Account account = getAccount(changeAccountVerificationStatusDtoRequest.getAccountEmail());
        boolean isVerified = changeAccountVerificationStatusDtoRequest.isVerificationStatus();

        if (isVerified) {
            if (!account.isVerified()) {
                account.setVerified(true);
            }
        } else {
            if (account.isVerified()) {
                account.setVerified(false);
            }
        }

        userRepository.save(account);
    }

    public boolean isNonEnabled(Principal principal) {
        String accountEmail = principal.getName();
        Account account = getAccount(accountEmail);
        return !account.isEnabled();
    }

    public boolean isNonLocked(Principal principal) {
        String accountEmail = principal.getName();
        Account account = getAccount(accountEmail);
        return !account.isNonLocked();
    }

    public boolean isNonFullyActivated(Principal principal) {
        String accountEmail = principal.getName();
        Account account = getAccount(accountEmail);
        return account.isVerified();
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

    private AccountDto mapToDTO(Account account) {
        AccountDto accountDto = new AccountDto();
        accountDto.setEmail(account.getEmail());
        accountDto.setPhone(account.getPhone());
        accountDto.setUsername(account.getUsername());
        accountDto.setFirstName(account.getFirstName());
        accountDto.setLastName(account.getLastName());
        accountDto.setFatherName(account.getFatherName());
        accountDto.setAddress(account.getAddress());
        accountDto.setZipCode(account.getZipCode());
        accountDto.setCity(account.getCity());
        accountDto.setCountry(account.getCountry());
        accountDto.setDocumentCountry(account.getDocumentCountry());
        return accountDto;
    }

    private boolean isTruePassword(Account account, String oldPassword) {
        String userOldPassword = account.getPassword();

        return encoder.matches(oldPassword, userOldPassword);
    }
}
