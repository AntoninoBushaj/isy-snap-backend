package com.isysnap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QrAuthorizeRequest {

    @NotBlank(message = "QR slug is required")
    private String qrSlug;

    @NotNull(message = "Timestamp is required")
    private Long timestamp;

    @NotBlank(message = "Signature is required")
    private String signature;

    @NotBlank(message = "Ephemeral key is required")
    private String ephemeralKey;
}