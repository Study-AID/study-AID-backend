ALTER TABLE app.exams
    DROP CONSTRAINT IF EXISTS chk_status;

ALTER TABLE app.exams
    ADD CONSTRAINT chk_status
        CHECK (status IN ('generate_in_progress', 'not_started', 'submitted', 'partially_graded', 'graded'));