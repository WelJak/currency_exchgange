package com.weljak.currencyexchange.service;

import com.weljak.currencyexchange.domain.BankAccountRepository;
import com.weljak.currencyexchange.domain.model.BalanceDetails;
import com.weljak.currencyexchange.domain.model.BankAccount;
import com.weljak.currencyexchange.domain.model.ExchangeDetails;
import com.weljak.currencyexchange.domain.model.exception.AccountNotFoundException;
import com.weljak.currencyexchange.domain.model.exception.ExternalServiceException;
import com.weljak.currencyexchange.domain.model.exception.InsufficientFundsException;
import com.weljak.currencyexchange.domain.model.exception.RateNotFoundException;
import com.weljak.currencyexchange.domain.model.external.ExchangeRateDetailsDto;
import com.weljak.currencyexchange.domain.model.external.Rate;
import com.weljak.currencyexchange.util.Endpoints;
import com.weljak.currencyexchange.webapi.request.CreateBankAccountRequest;
import com.weljak.currencyexchange.webapi.request.ExchangeCurrencyRequest;
import com.weljak.currencyexchange.webapi.response.CreateBankAccountResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class H2BankAccountService implements BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final RestTemplate restTemplate;

    private static final int SCALE = 2;

    @Override
    @Transactional
    public BalanceDetails getBalance(String id) {
        Optional<BankAccount> accountOptional = bankAccountRepository.findByUuid(id);
        if (accountOptional.isEmpty()) {
            throw new AccountNotFoundException("Account not found");
        }
        return BalanceDetails.fromBankAccount(accountOptional.get());
    }

    @Override
    @Transactional
    public ExchangeDetails exchangeCurrency(String id, ExchangeCurrencyRequest request) {
        Optional<BankAccount> accountOptional = bankAccountRepository.findByUuid(id);
        if (accountOptional.isEmpty()) {
            throw new AccountNotFoundException("Account not found");
        }
        BankAccount account = accountOptional.get();
        switch (request.getExchangeType()) {
            case PLN_TO_USD -> {
                if (account.getBalancePLN().compareTo(request.getAmountToExchange()) < 0) {
                    log.error("Not enough PLN on account");
                    throw new InsufficientFundsException("Not enough funds on account to exchange currency");
                }
                BigDecimal currencyRate = getPLNtoUSDCurrencyRate();
                BigDecimal balancePLNUpdated = account.getBalancePLN().subtract(request.getAmountToExchange());
                BigDecimal balanceUSDUpdated = account.getBalanceUSD().add(request.getAmountToExchange().divide(currencyRate, RoundingMode.CEILING));
                return updateBankAccountAndReturnExchangeDetails(account, balancePLNUpdated, balanceUSDUpdated);
            }
            case USD_TO_PLN -> {
                if (account.getBalanceUSD().compareTo(request.getAmountToExchange()) < 0) {
                    log.error("Not enough usd on account");
                    throw new InsufficientFundsException("Not enough funds on account to exchange currency");
                }
                BigDecimal currencyRate = getUSDtoPLNCurrencyRate();
                BigDecimal balancePLNUpdated = account.getBalancePLN().add(request.getAmountToExchange().multiply(currencyRate).setScale(SCALE, RoundingMode.CEILING));
                BigDecimal balanceUSDUpdated = account.getBalanceUSD().subtract(request.getAmountToExchange());
                return updateBankAccountAndReturnExchangeDetails(account, balancePLNUpdated, balanceUSDUpdated);
            }
            default -> {
                log.error("Given exchange type not recognized");
                throw new RateNotFoundException();
            }
        }
    }

    @Override
    @Transactional
    public CreateBankAccountResponse createBankAccount(CreateBankAccountRequest form) {
        String uuid = UUID.randomUUID().toString();
        BankAccount account = new BankAccount(
                uuid,
                form.getName(),
                form.getSurname(),
                form.getInitialBalancePLN(),
                BigDecimal.ZERO
        );
        bankAccountRepository.save(account);
        return new CreateBankAccountResponse(uuid, form.getInitialBalancePLN());
    }

    private BigDecimal getPLNtoUSDCurrencyRate() {
        ResponseEntity<ExchangeRateDetailsDto> response = restTemplate.getForEntity(Endpoints.EXTERNAL_USD_CURRENCY_RATE_ENDPOINT, ExchangeRateDetailsDto.class);
        if (response.getStatusCode().isError()) {
            log.error("Error occurred during calling nbp api");
            throw new ExternalServiceException("Error occurred during calling external api");
        }
        return BigDecimal.valueOf(extractRateFromResponse(response).getAsk()).setScale(SCALE, RoundingMode.CEILING);
    }

    private BigDecimal getUSDtoPLNCurrencyRate() {
        ResponseEntity<ExchangeRateDetailsDto> response = restTemplate.getForEntity(Endpoints.EXTERNAL_USD_CURRENCY_RATE_ENDPOINT, ExchangeRateDetailsDto.class);
        if (response.getStatusCode().isError()) {
            log.error("Error occurred during calling nbp api");
            throw new ExternalServiceException("Error occurred during calling external api");
        }
        return BigDecimal.valueOf(extractRateFromResponse(response).getBid()).setScale(SCALE, RoundingMode.CEILING);
    }

    private Rate extractRateFromResponse(ResponseEntity<ExchangeRateDetailsDto> response) {
        return response.getBody().getRates().stream().findFirst().orElseThrow(RateNotFoundException::new);
    }

    private ExchangeDetails updateBankAccountAndReturnExchangeDetails(BankAccount account, BigDecimal balancePLNUpdated, BigDecimal balanceUSDUpdated) {
        BankAccount updated = new BankAccount(
                account.getUuid(),
                account.getName(),
                account.getSurname(),
                balancePLNUpdated,
                balanceUSDUpdated
        );
        bankAccountRepository.save(updated);
        return new ExchangeDetails(balancePLNUpdated, balanceUSDUpdated);
    }
}
