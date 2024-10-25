package com.dws.challenge.domain;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferRequest {
    @NotNull
    @NotEmpty
    private String accountFrom;

    @NotNull
    @NotEmpty
    private String accountTo;

    @NotNull
    @Min(value = 1, message = "Amount must be positive.")
    private BigDecimal amount;

    // Getters and setters
    public String getAccountFrom() {
        return accountFrom;
    }

    public void setAccountFrom(String accountFrom) {
        this.accountFrom = accountFrom;
    }

    public String getAccountTo() {
        return accountTo;
    }

    public void setAccountTo(String accountTo) {
        this.accountTo = accountTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
