package com.wtd.service;

import com.wtd.dto.ChatRequestDto;
import com.wtd.vo.ChatResponseVo;

public interface ChatService {
    ChatResponseVo chat(ChatRequestDto requestDto);
}
