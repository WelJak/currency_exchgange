package com.weljak.currencyexchange.domain.model.external;

import lombok.Data;

import java.util.List;

@Data
public class ExchangeRateDetailsDto {
    private String code;
    private String currency;
    private List<Rate> rates;
    private String table;

}
