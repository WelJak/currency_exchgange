package com.weljak.currencyexchange.webapi.request;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class CreateBankAccountRequest {
    String name;
    String surname;
    BigDecimal initialBalancePLN;
}
