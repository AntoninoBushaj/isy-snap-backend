package com.isysnap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTableRequest {

    @NotBlank(message = "Table code is required")
    @Size(max = 50, message = "Table code must not exceed 50 characters")
    private String code;
}