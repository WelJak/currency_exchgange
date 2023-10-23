package com.weljak.currencyexchange.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class BalanceDetails {
    String name;
    String surname;
    BigDecimal balancePLN;
    BigDecimal balanceUSD;

    public static BalanceDetails fromBankAccount(BankAccount bankAccount) {
        return new BalanceDetails(
                bankAccount.getName(),
                bankAccount.getSurname(),
                bankAccount.getBalancePLN(),
                bankAccount.getBalanceUSD()
        );
    }
}
