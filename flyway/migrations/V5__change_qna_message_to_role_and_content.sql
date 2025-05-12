ALTER TABLE app.qna_chat_messages
    ADD COLUMN role VARCHAR(10) NOT NULL;
ALTER TABLE app.qna_chat_messages
    RENAME COLUMN message TO content;