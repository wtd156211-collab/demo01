package com.wtd.service;

import com.wtd.vo.ChatResponseVo;

public interface ChatService {
    ChatResponseVo chat(String message, String model);
}
