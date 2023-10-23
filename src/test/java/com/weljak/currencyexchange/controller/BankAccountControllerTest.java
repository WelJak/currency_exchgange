package com.weljak.currencyexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weljak.currencyexchange.util.Endpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(controllers = BankAccountController.class)
public class BankAccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankAccountService bankAccountService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnStatusOkWhenCreateUserInputIsValid() {
        //given
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new BankAccountRequest(testName, testSurname, initialBalancePLN);

        //when
        String testUUID = UUID.randomUUID().toString();
        BigDecimal initialBalanceUSD = BigDecimal.ZERO;
        when(bankAccountService.createAccount(testName, testSurname, initialBalancePLN)).thenReturn(new CreateAccountResponse(testUUID, initialBalancePLN, initialBalanceUSD));

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasEmptyName() {
        //given
        String testName = "";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new BankAccountRequest(testName, testSurname, initialBalancePLN);


        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasEmptySurname() {
        //given
        String testName = "John";
        String testSurname = "";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new BankAccountRequest(testName, testSurname, initialBalancePLN);


        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasNegativeInitialValue() {
        //given
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.valueOf(-123.00).setScale(2, RoundingMode.CEILING);
        CreateBankAccountRequest request = new BankAccountRequest(testName, testSurname, initialBalancePLN);


        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusOkWhenCheckBalanceInputIsValid() throws Exception {
        //given
        String testUUID = UUID.randomUUID().toString();

        //when
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        BigDecimal initialBalanceUSD = BigDecimal.ZERO;
        when(bankAccountService.getBalance(testUUID)).thenReturn(new GetBalanceResponse(testName, testSurname, initialBalancePLN, initialBalanceUSD));

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_GET_BALANCE_ENDPOINT, testUUID).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCheckBalanceInputIsBlank() {
        //given
        String testUUID = "   ";


        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_GET_BALANCE_ENDPOINT, testUUID).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusOkWhenExchangeCurrencyRequestIsValid() {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExcchangeCurrencyRequest(testUUID, exchangeType, amountToExchange);

        //when
        String testName = "John";
        String testSurname = "Doe";
        when(bankAccountService.exchangeCurrency(testUUID, exchangeType, amountToExchange)).thenReturn(new ExchangeCurrencyResponse(testUUID, BigDecimal.ZERO, amountToExchange));

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusBadRequestWhenAmountToExchangeIsNegative(){
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(-123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExcchangeCurrencyRequest(testUUID, exchangeType, amountToExchange);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenIdIsBlank(){
        //given
        String testUUID = "";
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExcchangeCurrencyRequest(testUUID, exchangeType, amountToExchange);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }
}
