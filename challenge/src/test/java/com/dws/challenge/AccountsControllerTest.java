package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Mock // Mocking NotificationService
  private NotificationService notificationService;

  @BeforeEach
  void prepareMockMvc() {
    MockitoAnnotations.openMocks(this); // Initialize mocks
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
            .andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
            .andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"Id-123\",\"balance\":1000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"balance\":1000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"Id-123\"}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"Id-123\",\"balance\":-1000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"accountId\":\"\",\"balance\":1000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
            .andExpect(status().isOk())
            .andExpect(content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }

  @Test
  void transferMoney() throws Exception {
    String accountFromId = "Id-123";
    String accountToId = "Id-456";

    // Create two accounts for transferring money
    this.accountsService.createAccount(new Account(accountFromId, new BigDecimal("1000")));
    this.accountsService.createAccount(new Account(accountToId, new BigDecimal("500")));

    // Prepare the transfer request
    String transferRequest = String.format("{\"accountFrom\":\"%s\", \"accountTo\":\"%s\", \"amount\":300}", accountFromId, accountToId);

    // Perform the transfer
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferRequest))
            .andExpect(status().isOk());

    // Verify the account balances after transfer
    Account accountFrom = accountsService.getAccount(accountFromId);
    Account accountTo = accountsService.getAccount(accountToId);

    assertThat(accountFrom.getBalance()).isEqualByComparingTo("700.00"); // 1000 - 300
    assertThat(accountTo.getBalance()).isEqualByComparingTo("800.00"); // 500 + 300
  }

  @Test
  void transferMoneyInsufficientFunds() throws Exception {
    String accountFromId = "Id-123";
    String accountToId = "Id-456";

    // Create two accounts for transferring money
    this.accountsService.createAccount(new Account(accountFromId, new BigDecimal("100")));
    this.accountsService.createAccount(new Account(accountToId, new BigDecimal("500")));

    // Prepare the transfer request with an amount greater than the balance
    String transferRequest = String.format("{\"accountFrom\":\"%s\", \"accountTo\":\"%s\", \"amount\":200}", accountFromId, accountToId);

    // Perform the transfer and expect a bad request status
    this.mockMvc.perform(post("/v1/accounts/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferRequest))
            .andExpect(status().isBadRequest());
  }
}
