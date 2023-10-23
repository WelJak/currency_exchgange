package com.weljak.currencyexchange.util;

public class Endpoints {
    public static final String BANK_ACCOUNT_BASE_URL = "/bank/account";
    public static final String BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT = BANK_ACCOUNT_BASE_URL + "/create";
    public static final String BANK_ACCOUNT_GET_BALANCE_ENDPOINT = BANK_ACCOUNT_BASE_URL + "/{id}/balance";
    public static final String BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT = BANK_ACCOUNT_BASE_URL + "/{id}/currency/exchange";
}
