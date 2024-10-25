package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.service.AccountsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Unit tests for AccountsService class.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  private Account account;

  @BeforeEach
  void setUp() {
    account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
  }

  /**
   * Test case for successfully adding an account.
   */
  @Test
  void addAccount() {
    this.accountsService.createAccount(account);
    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  /**
   * Test case for failing to add a duplicate account.
   */
  @Test
  void addAccount_failsOnDuplicateId() {
    // Create the account for the first time
    this.accountsService.createAccount(account);

    // Attempt to create the same account again and expect a DuplicateAccountIdException
    DuplicateAccountIdException thrown = assertThrows(DuplicateAccountIdException.class, () -> {
      this.accountsService.createAccount(account);
    });

    assertThat(thrown.getMessage()).isEqualTo("Account id Id-123 already exists!");
  }

  /**
   * Test case for retrieving a non-existing account.
   */
  @Test
  void getNonExistingAccount() {
    // Attempt to retrieve an account that has not been created
    Account retrievedAccount = this.accountsService.getAccount("NonExistingId");
    assertThat(retrievedAccount).isNull(); // Assuming getAccount returns null for non-existing IDs
  }

  /**
   * Test case for creating an account with an invalid ID.
   */
  @Test
  void addAccount_withInvalidId() {
    Account invalidAccount = new Account(null); // Invalid ID

    assertThrows(IllegalArgumentException.class, () -> {
      this.accountsService.createAccount(invalidAccount);
    });
  }
}
