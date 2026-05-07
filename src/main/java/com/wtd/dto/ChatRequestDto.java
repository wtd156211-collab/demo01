package com.wtd.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequestDto {
    @NotBlank(message = "问题不能为空")
    private String message;

    /**
     * 可选：deepseek-v4-flash / deepseek-v4-pro
     */
    private String model;
}
