ALTER TABLE quizzes
    ADD COLUMN contents_generated_at TIMESTAMP DEFAULT NULL;

ALTER TABLE quizzes
    DROP COLUMN IF EXISTS referenced_lectures;

ALTER TABLE exams
    ADD COLUMN contents_generated_at TIMESTAMP DEFAULT NULL;
