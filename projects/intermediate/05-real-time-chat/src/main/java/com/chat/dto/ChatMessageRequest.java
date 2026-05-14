package com.chat.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {

    private String roomId;
    private String sender;
    private String content;
    private String type;
}
