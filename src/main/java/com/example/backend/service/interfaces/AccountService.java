package com.example.backend.service.interfaces;

import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.*;
import com.example.backend.entity.Account;
import com.example.backend.error.AccountAlreadyExistsException;
import com.example.backend.error.AccountNotFoundException;
import com.example.backend.error.OldPasswordMissmatchException;
import com.example.backend.error.PasswordMissmatchException;
import com.example.backend.util.SimplePage;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public interface AccountService {
    String registerUser(SignupRequest signupRequest) throws AccountAlreadyExistsException;

    String loginUser(Authentication authentication);

    Account getAccount(String email) throws AccountNotFoundException;

    AccountDto getAccountDto(String email) throws AccountNotFoundException;

    Account getAccountByPhone(String phone) throws AccountNotFoundException;

    AccountDto getAccountByEmailOrPhone(FindAccountDtoRequest findAccountDtoRequest) throws AccountNotFoundException;

    String loginOAuthGoogle(IdTokenRequest requestBody) throws AccountNotFoundException;

    Account createOrUpdateUser(Account account);

    AccountDto updateAccountInformation(String accountEmail, AccountInfoRequest accountInfoRequest) throws AccountNotFoundException;

    SimplePage<AccountDto> findAll(final Pageable pageable);

    AccountDto changePassword(ChangePasswordDtoRequest changePasswordDtoRequest, Principal principal) throws AccountNotFoundException, PasswordMissmatchException, OldPasswordMissmatchException;

    AccountDto createNewPassword(CreateNewPasswordDtoRequest createNewPasswordDtoRequest, AccountDto accountDto) throws AccountNotFoundException, PasswordMissmatchException;

    void updateDocumentLink(String email, String link, int page) throws AccountNotFoundException;

    void activateUserAccount(String email) throws AccountNotFoundException;

    void lockedUserAccount(String email) throws AccountNotFoundException;

    void changeAccountVerificationStatus(ChangeAccountVerificationStatusDtoRequest changeAccountVerificationStatusDtoRequest) throws AccountNotFoundException;

    boolean isNonEnabled(Principal principal) throws AccountNotFoundException;

    boolean isNonLocked(Principal principal) throws AccountNotFoundException;

    boolean isNonFullyActivated(Principal principal) throws AccountNotFoundException;
}
