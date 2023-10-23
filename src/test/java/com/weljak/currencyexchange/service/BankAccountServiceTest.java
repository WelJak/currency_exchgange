package com.weljak.currencyexchange.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

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
        CreateBankAccountRequest request = new BankAccountRequest(testName, testSurname, initialBalancePLN);

        //when
        CreateAccountResponse result = bankAccountService.createBanAccount(request);

        //then
        verify(repository).save(any());
        assertEquals(initialBalancePLN, result.getPLNBalance());
        assertEquals(BigDecimal.ZERO, result.getUSDBalance());
        assertNotNull(result.getId());
    }

    @Test
    void shouldExchangeCurrency() {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(testUUID, exchangeType, amountToExchange);

        //when
        when(restTemplate.getForEntity(any(), any())).thenReturn(new BnpApiResponseDto());
        when(repository.findById(any())).thenReturn(new BankAccount(testUUID, "testName", "testSurname", amountToExchange, BigDecimal.ZERO));
        BalanceDetails result = bankAccountService.exchangeCurrency(exchangeCurrencyRequest);

        //then
        verify(restTemplate).postForEntity(any(), any());
        verify(repository).save();
        verify(repository).findById(testUUID);
        assertEquals(testUUID, result.getId());
        assertEquals(BigDecimal.ZERO, result.getBalancePLN());
        assertEquals(amountToExchange, result.getBalanceUSD());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(testUUID, exchangeType, amountToExchange);

        //when
        when(repository.findById(any())).thenReturn(new BankAccount(testUUID, "testName", "testSurname", amountToExchange.subtract(BigDecimal.TEN), BigDecimal.ZERO));
        assertThrows(Exception.class, bankAccountService.exchangeCurrency(exchangeCurrencyRequest));
    }

    @Test
    void shouldReturnAccountBalance() {
        // given
        String testUUID = UUID.randomUUID().toString();

        //when
        when(repository.findById(testUUID)).thenReturn(Optional.of(new BankAccount(testUUID, "testName", "testSurname", amountToExchange.subtract(BigDecimal.TEN), BigDecimal.ZERO)));
        BankAccount bankAccount = bankAccountService.getBalance(testUUID);

        //then
        assertNotNull(bankAccount);
    }

    @Test
    void shouldThrowExceptionWhenBankAccountDoesNotExist() {
        // given
        String testUUID = UUID.randomUUID().toString();

        //when
        when(repository.findById(testUUID)).thenReturn(Optional.empty());
        assertThrows(Exception.class, bankAccountService.getBalance(testUUID));
    }
}
