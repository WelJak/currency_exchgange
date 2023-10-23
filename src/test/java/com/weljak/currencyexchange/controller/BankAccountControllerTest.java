package com.weljak.currencyexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weljak.currencyexchange.domain.model.BalanceDetails;
import com.weljak.currencyexchange.domain.model.ExchangeDetails;
import com.weljak.currencyexchange.domain.model.ExchangeType;
import com.weljak.currencyexchange.service.BankAccountService;
import com.weljak.currencyexchange.util.Endpoints;
import com.weljak.currencyexchange.webapi.BankAccountController;
import com.weljak.currencyexchange.webapi.request.CreateBankAccountRequest;
import com.weljak.currencyexchange.webapi.request.ExchangeCurrencyRequest;
import com.weljak.currencyexchange.webapi.response.CreateBankAccountResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void shouldReturnStatusOkWhenCreateUserInputIsValid() throws Exception {
        //given
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new CreateBankAccountRequest(testName, testSurname, initialBalancePLN);

        //when
        String testUUID = UUID.randomUUID().toString();
        when(bankAccountService.createBankAccount(request)).thenReturn(new CreateBankAccountResponse(testUUID, initialBalancePLN));

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasEmptyName() throws Exception {
        //given
        String testName = "";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new CreateBankAccountRequest(testName, testSurname, initialBalancePLN);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasEmptySurname() throws Exception {
        //given
        String testName = "John";
        String testSurname = "";
        BigDecimal initialBalancePLN = BigDecimal.ZERO;
        CreateBankAccountRequest request = new CreateBankAccountRequest(testName, testSurname, initialBalancePLN);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCreateUserInputHasNegativeInitialValue() throws Exception {
        //given
        String testName = "John";
        String testSurname = "Doe";
        BigDecimal initialBalancePLN = BigDecimal.valueOf(-123.00).setScale(2, RoundingMode.CEILING);
        CreateBankAccountRequest request = new CreateBankAccountRequest(testName, testSurname, initialBalancePLN);


        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_CREATE_ACCOUNT_ENDPOINT).content(objectMapper.writeValueAsString(request)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
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
        when(bankAccountService.getBalance(testUUID)).thenReturn(new BalanceDetails(testName, testSurname, initialBalancePLN, initialBalanceUSD));

        //then
        mockMvc.perform(get(Endpoints.BANK_ACCOUNT_GET_BALANCE_ENDPOINT, testUUID).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusBadRequestWhenCheckBalanceInputIsBlank() throws Exception {
        //given
        String testUUID = "   ";


        //then
        mockMvc.perform(get(Endpoints.BANK_ACCOUNT_GET_BALANCE_ENDPOINT, testUUID).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusOkWhenExchangeCurrencyRequestIsValid() throws Exception {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(exchangeType, amountToExchange);

        //when
        when(bankAccountService.exchangeCurrency(testUUID, exchangeCurrencyRequest)).thenReturn(new ExchangeDetails(BigDecimal.ZERO, amountToExchange));

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void shouldReturnStatusBadRequestWhenAmountToExchangeIsNegative() throws Exception {
        //given
        String testUUID = UUID.randomUUID().toString();
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(-123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(exchangeType, amountToExchange);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnStatusBadRequestWhenIdIsBlank() throws Exception {
        //given
        String testUUID = "  ";
        ExchangeType exchangeType = ExchangeType.PLN_TO_USD;
        BigDecimal amountToExchange = BigDecimal.valueOf(123.23).setScale(2, RoundingMode.CEILING);
        ExchangeCurrencyRequest exchangeCurrencyRequest = new ExchangeCurrencyRequest(exchangeType, amountToExchange);

        //then
        mockMvc.perform(post(Endpoints.BANK_ACCOUNT_EXCHANGE_CURRENCY_ENDPOINT, testUUID).content(objectMapper.writeValueAsString(exchangeCurrencyRequest)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }
}
