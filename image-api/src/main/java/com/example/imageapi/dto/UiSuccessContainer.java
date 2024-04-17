package com.example.imageapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UiSuccessContainer {
    boolean success;
    String message;
}
