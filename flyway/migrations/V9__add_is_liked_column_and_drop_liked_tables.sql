ALTER TABLE app.quiz_items
    ADD COLUMN is_liked BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE app.exam_items
    ADD COLUMN is_liked BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE app.qna_chat_messages
    ADD COLUMN is_liked BOOLEAN DEFAULT FALSE NOT NULL;
DROP TABLE IF EXISTS app.liked_quiz_items;
DROP TABLE IF EXISTS app.liked_exam_items;
DROP TABLE IF EXISTS app.liked_qna_chat_messages;