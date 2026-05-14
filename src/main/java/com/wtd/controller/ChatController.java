package com.wtd.controller;

import com.wtd.common.Result;
import com.wtd.dto.ChatRequestDto;
import com.wtd.service.ChatService;
import com.wtd.vo.ChatResponseVo;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Result<ChatResponseVo> chat(@Valid @RequestBody ChatRequestDto requestDto) {
        log.info("收到聊天请求, sessionId={}, model={}, message={}",
                requestDto.getSessionId(), requestDto.getModel(), requestDto.getMessage());
        ChatResponseVo responseVo = chatService.chat(requestDto);
        return Result.success(responseVo);
    }
}
