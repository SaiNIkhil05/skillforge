package com.skillforge.dto;

import java.util.ArrayList;
import java.util.List;

public class ChatRequestDTO {
    private List<ChatMessageDTO> messages = new ArrayList<>();
    private String customApiKey;

    public ChatRequestDTO() {
    }

    public List<ChatMessageDTO> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessageDTO> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
    }

    public String getCustomApiKey() {
        return customApiKey;
    }

    public void setCustomApiKey(String customApiKey) {
        this.customApiKey = customApiKey;
    }
}
