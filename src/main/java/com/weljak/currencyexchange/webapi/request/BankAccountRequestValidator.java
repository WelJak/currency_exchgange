package com.weljak.currencyexchange.webapi.request;

import java.math.BigDecimal;

public class BankAccountRequestValidator {
    public static boolean validateCreateAccountForm(CreateBankAccountRequest form) {
        if (form.getName().isEmpty() || form.getName().isBlank() || form.getSurname().isEmpty() || form.getSurname().isBlank())
            return false;
        return form.getInitialBalancePLN().compareTo(BigDecimal.ZERO) >= 0;
    }

    public static boolean validateId(String id) {
        return !id.isEmpty() && !id.isBlank();
    }

    public static boolean validateExchangeCurrencyRequest(String id, ExchangeCurrencyRequest request) {
        if (!validateId(id)) return false;
        return request.getAmountToExchange().compareTo(BigDecimal.ZERO) >= 0;
    }
}
