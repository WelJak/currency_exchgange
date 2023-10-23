package com.weljak.currencyexchange.domain.model.external;


import lombok.Data;

@Data
public class Rate {
    private Double ask;
    private Double bid;
    private String effectiveDate;
    private String no;
}
