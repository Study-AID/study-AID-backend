package com.example.api.service;

import com.example.api.service.dto.qna.QnaChatInput;
import com.example.api.service.dto.qna.QnaChatOutput;

public interface QnaChatService {
    QnaChatOutput ask(QnaChatInput input);
}