ALTER TABLE app.exams
    DROP CONSTRAINT IF EXISTS chk_status;

ALTER TABLE app.quizzes
    DROP CONSTRAINT IF EXISTS chk_quiz_status;

ALTER TABLE app.exams
    ADD CONSTRAINT chk_status
    CHECK (status IN ('generate_in_progress', 'not_started', 'submitted', 'graded'));

ALTER TABLE app.quizzes
    ADD CONSTRAINT chk_quiz_status
    CHECK (status IN ('generate_in_progress', 'not_started', 'submitted', 'graded'));