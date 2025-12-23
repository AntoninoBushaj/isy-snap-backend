package com.isysnap.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SuccessResponse {
    private Boolean success;

    public static SuccessResponse of(Boolean success) {
        return new SuccessResponse(success);
    }
}