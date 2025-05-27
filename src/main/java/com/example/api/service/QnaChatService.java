package com.example.api.service;

import com.example.api.service.dto.qna.*;

public interface QnaChatService {
    QnaChatMessageOutput ask(QnaChatMessageInput input);
    CreateQnaChatOutput createQnaChat(CreateQnaChatInput input);
    ReadQnaChatOutput getMessages(ReadQnaChatInput input);
    GetQnaChatIdOutput getQnaChatId(GetQnaChatIdInput input);

    ReadQnaChatOutput getLikedMessages(GetLikedMessagesInput input);
    void likeMessage(LikeMessageInput input);
    void unlikeMessage(UnlikeMessageInput input); 
}