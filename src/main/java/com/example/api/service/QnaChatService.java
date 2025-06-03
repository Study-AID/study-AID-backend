package com.example.api.service;

import com.example.api.service.dto.qna.*;

public interface QnaChatService {
    QnaChatMessageOutput ask(QnaChatMessageInput input);
    CreateQnaChatOutput createQnaChat(CreateQnaChatInput input);
    GetQnaChatMessagesOutput getMessages(GetQnaChatMessagesInput input);
    GetQnaChatIdOutput getQnaChatId(GetQnaChatIdInput input);

    GetLikedMessagesOutput getLikedMessages(GetLikedMessagesInput input);
    ToggleLikeMessageOutput toggleLikeMessage(ToggleLikeMessageInput input);
}