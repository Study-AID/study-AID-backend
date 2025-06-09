ALTER TABLE app.qna_chat
    ADD CONSTRAINT uk_qna_chat_lecture_user
        UNIQUE (lecture_id, user_id);