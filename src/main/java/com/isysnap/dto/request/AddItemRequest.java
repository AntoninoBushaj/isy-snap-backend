package com.isysnap.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class AddItemRequest {
    // sessionId, guestToken extracted from JWT in Authorization header
    private String menuItemId;
    private Integer quantity;
    private List<String> optionIds;
    private String notes;
}