package com.box.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ChatClient chatClient;

    public AiController(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestBody List<Map<String, Object>> messages) {
        System.out.println("收到消息: " + messages);

        String userMessage = String.valueOf(messages.get(messages.size() - 1).get("text"));
        System.out.println("用户消息: " + userMessage);

        return chatClient.prompt()
                .system("你是一个校园论坛的AI助手,如若用户没有要求,请用中文回答")
                .user(userMessage)
                .stream()
                .content();
    }
}