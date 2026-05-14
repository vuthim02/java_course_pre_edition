package com.chat.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private Long id;
    private String roomId;
    private String sender;
    private String content;
    private String type;
    private LocalDateTime timestamp;
}
