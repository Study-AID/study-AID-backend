package com.example.api.service;

import com.example.api.service.dto.qna.*;

public interface QnaChatService {
    QnaChatMessageOutput ask(QnaChatMessageInput input);
    CreateQnaChatOutput createQnaChat(CreateQnaChatInput input);
    ReadQnaChatOutput readQnaChat(ReadQnaChatInput input);
}