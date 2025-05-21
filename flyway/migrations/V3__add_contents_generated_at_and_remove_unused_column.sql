ALTER TABLE app.quizzes
    ADD COLUMN contents_generated_at TIMESTAMP DEFAULT NULL;

ALTER TABLE app.quizzes
    DROP COLUMN IF EXISTS referenced_lectures;

ALTER TABLE app.exams
    ADD COLUMN contents_generated_at TIMESTAMP DEFAULT NULL;
