package com.weljak.currencyexchange.webapi.exception;

import lombok.Value;

@Value
public class FailResponse {
    Integer code;
    String message;
}
