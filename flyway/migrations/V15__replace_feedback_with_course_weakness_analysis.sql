ALTER TABLE app.quiz_results
    DROP COLUMN IF EXISTS feedback;

ALTER TABLE app.exam_results
    DROP COLUMN IF EXISTS feedback;

ALTER TABLE app.courses
    ADD COLUMN IF NOT EXISTS course_weakness_analysis jsonb;