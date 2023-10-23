package com.weljak.currencyexchange.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weljak.currencyexchange.domain.BankAccountRepository;
import com.weljak.currencyexchange.domain.model.BalanceDetails;
import com.weljak.currencyexchange.domain.model.BankAccount;
import com.weljak.currencyexchange.domain.model.ExchangeDetails;
import com.weljak.currencyexchange.domain.model.ExchangeType;
import com.weljak.currencyexchange.domain.model.exception.AccountNotFoundException;
import com.weljak.currencyexchange.domain.model.exception.InsufficientFundsException;
import com.weljak.currencyexchange.domain.model.external.ExchangeRateDetailsDto;
import com.weljak.currencyexchange.webapi.request.CreateBankAccountRequest;
import com.weljak.currencyexchange.webapi.request.ExchangeCurrencyRequest;
import com.weljak.currencyexchange.webapi.response.CreateBankAccountResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import static com.weljak.currencyexchange.util.Endpoints.EXTERNAL_USD_CURRENCY_RATE_ENDPOINT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
public class BankAccountServiceTest {
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private BankAccountRepository repository;

    @Autowired
    BankAccountService bankAccountService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void teardown() {
        reset(restTemplate, repository);
    }

    @Test
    void shouldCreateNewBankAccount() {
        //given
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new CreateBankAccountRequest(testName, testSurname, initialBalancePLN);

        //when
        CreateBankAccountResponse result = bankAccountService.createBankAccount(request);

        //then
        verify(repository).save(any());
        assertEquals(initialBalancePLN, result.getInitialPLNBalance());
        assertNotNull(result.getId());
    }

    @Test
    void shouldExchangeCurrency() {
        //given
        int scale = 2;
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(scale, RoundingMode.CEILING);
        BigDecimal expectedUsdBalance = BigDecimal.valueOf(29.00).setScale(scale, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(exchangeType, amountToExchange);

        //when
        when(restTemplate.getForEntity(EXTERNAL_USD_CURRENCY_RATE_ENDPOINT, ExchangeRateDetailsDto.class)).thenReturn(ResponseEntity.ok(getStubbedExchangeRate()));
        when(repository.findByUuid(testUUID)).thenReturn(Optional.of(new BankAccount(testUUID, "testName", "testSurname", amountToExchange, BigDecimal.ZERO)));
        ExchangeDetails result = bankAccountService.exchangeCurrency(testUUID, exchangeCurrencyRequest);

        //then
        verify(restTemplate).getForEntity(EXTERNAL_USD_CURRENCY_RATE_ENDPOINT, ExchangeRateDetailsDto.class);
        verify(repository).save(any());
        verify(repository).findByUuid(testUUID);
        assertEquals(BigDecimal.ZERO.setScale(scale, RoundingMode.CEILING), result.getNewBalancePLN());
        assertEquals(expectedUsdBalance, result.getNewBalanceUSD());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(exchangeType, amountToExchange);

        //when
        when(repository.findByUuid(testUUID)).thenReturn(Optional.of(new BankAccount(testUUID, "testName", "testSurname", amountToExchange.subtract(BigDecimal.TEN), BigDecimal.ZERO)));
        assertThrows(InsufficientFundsException.class, () -> bankAccountService.exchangeCurrency(testUUID, exchangeCurrencyRequest));
    }

    @Test
    void shouldReturnAccountBalance() {
        // given
        String testUUID = UUID.randomUUID().toString();
        String testName = "testName";
        String testSurname = "testSurname";
        BigDecimal plnBalance = BigDecimal.valueOf(1222.31);
        BigDecimal usdBalance = BigDecimal.TEN;

        //when
        when(repository.findByUuid(testUUID)).thenReturn(Optional.of(new BankAccount(testUUID, testName, testSurname, plnBalance, usdBalance)));
        BalanceDetails balanceDetails = bankAccountService.getBalance(testUUID);

        //then
        assertNotNull(balanceDetails);
        assertEquals(testName, balanceDetails.getName());
        assertEquals(testSurname, balanceDetails.getSurname());
        assertEquals(plnBalance, balanceDetails.getBalancePLN());
        assertEquals(usdBalance, balanceDetails.getBalanceUSD());
    }

    @Test
    void shouldThrowExceptionWhenBankAccountDoesNotExist() {
        // given
        String testUUID = UUID.randomUUID().toString();

        //when
        when(repository.findById(testUUID)).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> bankAccountService.getBalance(testUUID));
    }

    private static ExchangeRateDetailsDto getStubbedExchangeRate() {
        String json = """
                {
                    "table": "C",
                    "currency": "dolar amerykański",
                    "code": "USD",
                    "rates": [
                        {
                            "no": "205/C/NBP/2023",
                            "effectiveDate": "2023-10-23",
                            "bid": 4.1640,
                            "ask": 4.2482
                        }
                    ]
                }
                """;
        try {
            return objectMapper.readValue(json, new TypeReference<ExchangeRateDetailsDto>() {
            });
        } catch (JsonProcessingException exception) {
            return null;
        }
    }
}
