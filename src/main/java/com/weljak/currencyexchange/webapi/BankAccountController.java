package com.weljak.currencyexchange.webapi;

import com.weljak.currencyexchange.domain.model.exception.RequestValidationException;
import com.weljak.currencyexchange.service.BankAccountService;
import com.weljak.currencyexchange.util.Endpoints;
import com.weljak.currencyexchange.webapi.request.BankAccountRequestValidator;
import com.weljak.currencyexchange.webapi.request.CreateBankAccountRequest;
import com.weljak.currencyexchange.webapi.request.ExchangeCurrencyRequest;
import com.weljak.currencyexchange.webapi.response.CreateBankAccountResponse;
import com.weljak.currencyexchange.webapi.response.ExchangeCurrencyResponse;
import com.weljak.currencyexchange.webapi.response.GetBalanceDetailsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT)
    ResponseEntity<CreateBankAccountResponse> createBankAccount(@RequestBody CreateBankAccountRequest request) {
        if (!BankAccountRequestValidator.validateCreateAccountForm(request)) {
            log.error("Validation error for request: {}", request);
            throw new RequestValidationException("Invalid input");
        }
        return new ResponseEntity<>(bankAccountService.createBankAccount(request), HttpStatus.CREATED);
    }

    @GetMapping(Endpoints.BANK_ACCOUNT_GET_BALANCE_ENDPOINT)
    ResponseEntity<GetBalanceDetailsResponse> getBalanceDetails(@PathVariable String id) {
        if (!BankAccountRequestValidator.validateId(id)) {
            log.error("Validation error for id: {}", id);
            throw new RequestValidationException("Invalid input parameter Id");
        }
        return ResponseEntity.ok(new GetBalanceDetailsResponse(bankAccountService.getBalance(id)));
    }

    @PostMapping(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT)
    ResponseEntity<ExchangeCurrencyResponse> exchangeCurrency(@PathVariable String id, @RequestBody ExchangeCurrencyRequest request) {
        if (!BankAccountRequestValidator.validateExchangeCurrencyRequest(id, request)) {
            log.error("Error occurred during validation of exchange currency request");
            throw new RequestValidationException("Invalid request");
        }
        return ResponseEntity.ok(new ExchangeCurrencyResponse(bankAccountService.exchangeCurrency(id, request)));
    }
}
