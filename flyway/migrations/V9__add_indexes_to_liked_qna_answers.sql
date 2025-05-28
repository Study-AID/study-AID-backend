-- Add performance indexes for liked_qna_answers table

CREATE INDEX IF NOT EXISTS idx_liked_qna_answers_chat_user
    ON app.liked_qna_answers (qna_chat_id, user_id);

CREATE INDEX IF NOT EXISTS idx_liked_qna_answers_chat_msg_user
    ON app.liked_qna_answers (qna_chat_id, message_id, user_id);