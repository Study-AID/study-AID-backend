package com.example.api.service;

import com.example.api.adapters.llm.ChatMessage;
import com.example.api.adapters.llm.LLMAdapter;
import com.example.api.entity.Lecture;
import com.example.api.external.LangchainClient;
import com.example.api.entity.QnaChat;
import com.example.api.entity.QnaChatMessage;
import com.example.api.entity.User;
import com.example.api.external.dto.langchain.MessageHistoryRequest;
import com.example.api.repository.LectureRepository;
import com.example.api.repository.QnaChatMessageRepository;
import com.example.api.repository.QnaChatRepository;
import com.example.api.repository.UserRepository;
import com.example.api.service.dto.qna.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class QnaChatServiceImpl implements QnaChatService {

    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final QnaChatRepository qnaChatRepository;
    private final QnaChatMessageRepository qnaChatMessageRepository;
    private final LangchainClient langchainClient;
    private final QnaQuestionRecommendService qnaQuestionRecommendService;
    private final LLMAdapter llmAdapter;

    @Override
    public CreateQnaChatOutput createQnaChat(CreateQnaChatInput input) {
        User user = userRepository.findById(input.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        Lecture lecture = lectureRepository.findById(input.getLectureId())
                .orElseThrow(() -> new RuntimeException("강의 자료 없음"));

        QnaChat chat = new QnaChat();
        chat.setUser(user);
        chat.setLecture(lecture);

        qnaChatRepository.save(chat);
        return new CreateQnaChatOutput(chat.getId());
    }
    
    @Override
    public QnaChatMessageOutput ask(QnaChatMessageInput input) {
        QnaChat chat = qnaChatRepository.findById(input.getChatId())
                .orElseThrow(() -> new IllegalArgumentException("QnA Chat not found"));
        UUID lectureId = chat.getLecture().getId();

        // 사용자 질문 저장
        QnaChatMessage userMsg = new QnaChatMessage();
        userMsg.setQnaChat(chat);
        userMsg.setUser(new User(input.getUserId()));
        userMsg.setQuestion(input.getQuestion());
        qnaChatMessageRepository.save(userMsg);

        // 출처 검색 및 저장
        List<String> referenceChunks = langchainClient.findReferences(lectureId, input.getQuestion(), 3);

        // 문맥 생성
        List<QnaChatMessage> history = qnaChatMessageRepository.findByQnaChatId(chat.getId());
        List<MessageHistoryRequest.MessageHistoryItem> formattedHistory = history.stream()
                .map(m -> new MessageHistoryRequest.MessageHistoryItem(
                        m.getQuestion(),
                        m.getAnswer()
                ))
                .filter(h -> h.getQuestion() != null && h.getAnswer() != null)
                .toList();

        List<ChatMessage> messageHistory = langchainClient.generateMessageHistory(input.getChatId(), lectureId, input.getQuestion(), formattedHistory);

        // GPT 호출 및 AI 답변 생성
        String answer = llmAdapter.ask(input.getQuestion(), referenceChunks, messageHistory);

        // AI 답변 저장
        QnaChatMessage botMsg = new QnaChatMessage();
        botMsg.setQnaChat(chat);
        botMsg.setUser(null); //점검 필요
        botMsg.setAnswer(answer);
        qnaChatMessageRepository.save(botMsg);

        // 추천 질문 생성
        List<String> recommendedQuestions = qnaQuestionRecommendService.recommendQuestions(input.getQuestion());

        // AI 최종 답변 반환
        return new QnaChatMessageOutput(
                input.getQuestion(),
                answer,
                referenceChunks,
                recommendedQuestions
        );
    }

    @Override
    public ReadQnaChatOutput readQnaChat(ReadQnaChatInput input) {
        QnaChat chat = qnaChatRepository.findById(input.getChatId())
                .orElseThrow(() -> new RuntimeException("채팅방 없음"));

        UUID lectureId = chat.getLecture().getId();

        if (!chat.getUser().getId().equals(input.getUserId())) {
            throw new RuntimeException("권한 없음");
        }

        List<ReadQnaChatOutput.MessageItem> messages = qnaChatMessageRepository.findByQnaChatId(chat.getId()).stream()
                .map(m -> new ReadQnaChatOutput.MessageItem(m.getQuestion(), m.getAnswer()))
                .toList();

        return new ReadQnaChatOutput(messages);
    }
}
