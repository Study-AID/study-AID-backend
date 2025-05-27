CREATE TABLE IF NOT EXISTS app.liked_exam_items
(
    id          uuid PRIMARY KEY,
    exam_id     uuid      NOT NULL,
    question_id uuid      NOT NULL,
    user_id     uuid      NOT NULL,

    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (exam_id) REFERENCES app.exams (id),
    FOREIGN KEY (question_id) REFERENCES app.exam_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

-- Create index for efficient queries
CREATE INDEX IF NOT EXISTS idx_liked_exam_items_exam_created_at
    ON app.liked_exam_items (exam_id, created_at);