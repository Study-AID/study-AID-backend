CREATE INDEX CONCURRENTLY IF NOT EXISTS 
    idx_exams_course_updated_at 
ON app.exams (course_id, updated_at)