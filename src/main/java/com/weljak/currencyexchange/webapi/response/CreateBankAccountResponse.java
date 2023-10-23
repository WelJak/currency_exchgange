package com.weljak.currencyexchange.webapi.response;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateBankAccountResponse {
    String id;
    BigDecimal initialPLNBalance;
}
