package com.weljak.currencyexchange.webapi.response;

import com.weljak.currencyexchange.domain.model.ExchangeDetails;
import lombok.Value;

@Value
public class ExchangeCurrencyResponse {
    ExchangeDetails details;
}
