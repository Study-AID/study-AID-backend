ALTER TABLE app.qna_chat_messages
    ADD COLUMN question TEXT,
    ADD COLUMN answer TEXT,
    DROP COLUMN message;