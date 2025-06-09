ALTER TABLE app.quiz_responses
    ADD COLUMN essay_criteria_analysis jsonb DEFAULT NULL;
ALTER TABLE app.exam_responses
    ADD COLUMN essay_criteria_analysis jsonb DEFAULT NULL;