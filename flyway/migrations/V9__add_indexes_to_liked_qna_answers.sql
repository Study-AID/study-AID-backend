-- Add performance indexes for liked_qna_answers table
-- This migration improves query performance for:
-- 1. Finding liked messages by chat and user (getLikedMessages)
-- 2. Checking if message is liked (existsByQnaChatIdAndQnaChatMessageIdAndUserId)
-- 3. Deleting likes (deleteByQnaChatIdAndQnaChatMessageIdAndUserId)

-- Index for querying user's liked messages in a specific chat
-- Used by: findByQnaChatIdAndUserId(), findByQnaChatIdAndUserIdWithMessage()
CREATE INDEX IF NOT EXISTS idx_liked_qna_answers_chat_user
    ON app.liked_qna_answers (qna_chat_id, user_id);

-- Composite index for like existence check and deletion operations
-- Used by: existsByQnaChatIdAndQnaChatMessageIdAndUserId(), deleteByQnaChatIdAndQnaChatMessageIdAndUserId()
CREATE INDEX IF NOT EXISTS idx_liked_qna_answers_chat_msg_user
    ON app.liked_qna_answers (qna_chat_id, message_id, user_id);