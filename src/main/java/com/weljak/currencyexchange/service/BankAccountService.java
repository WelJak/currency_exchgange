package com.weljak.currencyexchange.service;

import com.weljak.currencyexchange.domain.model.BalanceDetails;
import com.weljak.currencyexchange.domain.model.ExchangeDetails;
import com.weljak.currencyexchange.webapi.request.CreateBankAccountRequest;
import com.weljak.currencyexchange.webapi.request.ExchangeCurrencyRequest;
import com.weljak.currencyexchange.webapi.response.CreateBankAccountResponse;

public interface BankAccountService {
    BalanceDetails getBalance(String id);

    ExchangeDetails exchangeCurrency(String id, ExchangeCurrencyRequest request);

    CreateBankAccountResponse createBankAccount(CreateBankAccountRequest form);
}
