package com.isysnap.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAvailabilityRequest {

    @NotNull(message = "Available field is required")
    private Boolean available;
}