package com.example.api.service;

import com.example.api.external.RagClient;
import com.example.api.entity.QnaChat;
import com.example.api.entity.QnaChatMessage;
import com.example.api.entity.User;
import com.example.api.external.dto.rag.RagAnswer;
import com.example.api.repository.QnaChatMessageRepository;
import com.example.api.repository.QnaChatRepository;
import com.example.api.service.dto.qna.QnaChatInput;
import com.example.api.service.dto.qna.QnaChatOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class QnaChatServiceImpl implements QnaChatService {

    private final QnaChatRepository qnaChatRepository;
    private final QnaChatMessageRepository qnaChatMessageRepository;
    private final RagClient ragClient;
    private final QnaQuestionRecommendService qnaQuestionRecommendService;

    @Override
    public QnaChatOutput ask(QnaChatInput input) {
        QnaChat chat = qnaChatRepository.findById(input.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("QnA Chat not found"));

        // 사용자 질문 저장
        QnaChatMessage userMsg = new QnaChatMessage();
        userMsg.setQnaChat(chat);
        userMsg.setUser(new User(input.getUserId()));
        userMsg.setMessage(input.getQuestion());
        qnaChatMessageRepository.save(userMsg);

        // parsedText 가져오기
        String parsedText = chat.getLecture().getParsedText();

        // RAG 서버 호출 → 답변 생성
        RagAnswer ragAnswer = ragClient.query(input.getQuestion(), parsedText);

        // AI 답변 저장
        QnaChatMessage botMsg = new QnaChatMessage();
        botMsg.setQnaChat(chat);
        botMsg.setUser(null);  // AI
        botMsg.setMessage(ragAnswer.getAnswer());
        qnaChatMessageRepository.save(botMsg);

        // 추천 질문 생성
        List<String> recommendations = qnaQuestionRecommendService.recommendQuestions(input.getQuestion());

        // 응답 DTO 반환
        return new QnaChatOutput(
                input.getQuestion(),
                ragAnswer.getAnswer(),
                ragAnswer.getSource(),
                recommendations);
    }
}
