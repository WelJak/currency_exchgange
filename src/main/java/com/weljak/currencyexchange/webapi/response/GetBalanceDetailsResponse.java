package com.weljak.currencyexchange.webapi.response;

import com.weljak.currencyexchange.domain.model.BalanceDetails;
import lombok.Value;

@Value
public class GetBalanceDetailsResponse {
    BalanceDetails details;
}
