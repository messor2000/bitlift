package com.example.backend.service.interfaces;

import com.example.backend.dto.AccountDto;
import com.example.backend.dto.request.AccountInfoRequest;
import com.example.backend.dto.request.FindAccountDtoRequest;
import com.example.backend.dto.request.IdTokenRequest;
import com.example.backend.dto.request.SignupRequest;
import com.example.backend.entity.Account;
import com.example.backend.error.AccountAlreadyExistsException;
import com.example.backend.error.AccountNotFoundException;
import org.springframework.security.core.Authentication;

import java.security.Principal;

public interface AccountService {
    String registerUser(SignupRequest signupRequest) throws AccountAlreadyExistsException;

    String loginUser(Authentication authentication);

    Account getAccount(String email) throws AccountNotFoundException;

    AccountDto getAccountDto(String email) throws AccountNotFoundException;

    Account getAccountByPhone(String phone) throws AccountNotFoundException;

    Account getAccountForLogin(FindAccountDtoRequest findAccountDtoRequest) throws AccountNotFoundException;

    String loginOAuthGoogle(IdTokenRequest requestBody) throws AccountNotFoundException;

    Account createOrUpdateUser(Account account);

    AccountDto updateAccountInformation(String accountEmail, AccountInfoRequest accountInfoRequest) throws AccountNotFoundException;

    void updateDocumentLink(String email, String link, int page) throws AccountNotFoundException;

    void activateUserAccount(String email) throws AccountNotFoundException;

    void lockedUserAccount(String email);

    boolean isNonEnabled(Principal principal) throws AccountNotFoundException;

    boolean isNonLocked(Principal principal) throws AccountNotFoundException;
}
