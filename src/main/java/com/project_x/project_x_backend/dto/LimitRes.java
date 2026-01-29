package com.project_x.project_x_backend.dto;

import com.project_x.project_x_backend.enums.LimitStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LimitRes {
    private int dailyLimit;
    private int dailyUsed;
    private int monthlyLimit;
    private int monthlyUsed;
    private LimitStatus status;
}
