package com.weljak.currencyexchange.domain.model;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class ExchangeDetails {
    BigDecimal newBalancePLN;
    BigDecimal newBalanceUSD;
}
