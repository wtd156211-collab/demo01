package com.wtd.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRecord {
    /**
     * 会话编号，用于标识同一轮连续对话。
     */
    private String sessionId;

    /**
     * 用户问题。
     */
    private String userMessage;

    /**
     * 大模型回答。
     */
    private String assistantMessage;

    /**
     * 记录时间。
     */
    private LocalDateTime createTime;

    public String toPromptText() {
        return "用户: " + userMessage + "\n助手: " + assistantMessage;
    }
}
