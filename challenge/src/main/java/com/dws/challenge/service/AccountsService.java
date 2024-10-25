package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.TransferRequest;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.repository.AccountsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  private final AccountsRepository accountsRepository;
  private final NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
    this.accountsRepository = accountsRepository;
    this.notificationService = notificationService;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

  public void transfer(TransferRequest transferRequest) {
    synchronized (this) {
      // Validate positive amount
      if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Transfer amount must be positive.");
      }

      Account fromAccount = accountsRepository.getAccount(transferRequest.getAccountFrom());
      Account toAccount = accountsRepository.getAccount(transferRequest.getAccountTo());

      if (fromAccount == null || toAccount == null) {
        throw new IllegalArgumentException("Invalid account ID(s)");
      }

      if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
        throw new IllegalArgumentException("Insufficient balance");
      }

      // Perform the transfer
      fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
      toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

      // Send notifications
      notificationService.notifyAboutTransfer(fromAccount,
              "Transferred " + transferRequest.getAmount() + " to " + toAccount.getAccountId());
      notificationService.notifyAboutTransfer(toAccount,
              "Received " + transferRequest.getAmount() + " from " + fromAccount.getAccountId());
    }
  }

  // New method added
  public AccountsRepository getAccountsRepository() {
    return accountsRepository;
  }
}
