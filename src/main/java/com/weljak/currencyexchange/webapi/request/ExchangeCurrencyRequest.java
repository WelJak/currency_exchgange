package com.weljak.currencyexchange.webapi.request;

import com.weljak.currencyexchange.domain.model.ExchangeType;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ExchangeCurrencyRequest {
    ExchangeType exchangeType;
    BigDecimal amountToExchange;
}
