package com.isysnap.dto.request;

import lombok.Data;

@Data
public class QrAuthorizeRequest {
    private String qrSlug;
    private Long timestamp;
    private String signature;
    private String ephemeralKey;
}