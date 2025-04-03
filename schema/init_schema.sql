-- Create schema if not exists and set search path
CREATE SCHEMA IF NOT EXISTS app;
SET search_path TO app, public;

CREATE TABLE IF NOT EXISTS app.users
(
    id            uuid PRIMARY KEY,
    name          varchar(100) NOT NULL,
    email         varchar(255) NOT NULL UNIQUE,
    password_hash varchar(255),
    google_id     varchar(100),
    created_at    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    timestamp,
    last_login    timestamp,
    is_active     boolean      NOT NULL DEFAULT TRUE,
    auth_type     varchar(20)  NOT NULL DEFAULT 'email',

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_auth_type CHECK (auth_type IN ('email', 'google'))
);

CREATE INDEX IF NOT EXISTS idx_users_email ON app.users (email);
CREATE INDEX IF NOT EXISTS idx_users_google_id ON app.users (google_id);

CREATE TABLE IF NOT EXISTS app.semesters
(
    id           uuid PRIMARY KEY,
    user_id      uuid         NOT NULL,

    name         varchar(100) NOT NULL,
    year         int          NOT NULL,
    -- e.g., 'Spring', 'Fall' (NOTE(mj): make it enum?)
    season       varchar(20)  NOT NULL,
    start_date   date,
    end_date     date,
    target_grade float, -- e.g., 3.5

    created_at   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at   timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_season CHECK (season IN ('spring', 'summer', 'fall', 'winter')),

    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_semesters_user_created_at
    ON app.semesters (user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_semesters_user_updated_at
    ON app.semesters (user_id, updated_at);
CREATE INDEX IF NOT EXISTS idx_semesters_user_year_season
    ON app.semesters (user_id, year, season);

CREATE TABLE IF NOT EXISTS app.courses
(
    id                uuid PRIMARY KEY,
    semester_id       uuid         NOT NULL,
    user_id           uuid         NOT NULL,

    name              varchar(100) NOT NULL,
    target_grade      float, -- 목표학점, e.g., 3.5
    earned_grade      float, -- 취득학점, e.g., 3.5
    completed_credits int,   -- 이수학점, e.g., 3

    created_at        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        timestamp,

    FOREIGN KEY (semester_id) REFERENCES app.semesters (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_courses_semester_created_at
    ON app.courses (semester_id, created_at);
CREATE INDEX IF NOT EXISTS idx_courses_semester_updated_at
    ON app.courses (semester_id, updated_at);

CREATE TABLE IF NOT EXISTS app.course_assessments
(
    id         uuid PRIMARY KEY,
    course_id  uuid      NOT NULL,
    user_id    uuid      NOT NULL,

    title      varchar(255),
    score      float     NOT NULL,
    max_score  float     NOT NULL,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp,

    FOREIGN KEY (course_id) REFERENCES app.courses (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_course_assessments_course_created_at
    ON app.course_assessments (course_id, created_at);

CREATE TABLE IF NOT EXISTS app.course_activity_logs
(
    id               uuid PRIMARY KEY,
    course_id        uuid        NOT NULL,
    user_id          uuid        NOT NULL,

    activity_type    varchar(20) NOT NULL, -- e.g., 'add' 'update', 'delete'
    contents_type    varchar(20) NOT NULL, -- e.g., 'lecture', 'exam', 'quiz'
    activity_details jsonb       NOT NULL,

    created_at       timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (course_id) REFERENCES app.courses (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_course_activity_logs_course_created_at
    ON app.course_activity_logs (course_id, created_at);

CREATE TABLE IF NOT EXISTS app.exams
(
    id                  uuid PRIMARY KEY,
    course_id           uuid        NOT NULL,
    user_id             uuid        NOT NULL,

    title               varchar(255),
    status              varchar(20) NOT NULL,
    referenced_lectures uuid[],

    created_at          timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_status CHECK (status IN ('not_started', 'submitted', 'graded')),

    FOREIGN KEY (course_id) REFERENCES app.courses (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_exams_course_created_at
    ON app.exams (course_id, created_at);

CREATE TABLE IF NOT EXISTS app.exam_items
(
    id             uuid PRIMARY KEY,
    exam_id        uuid        NOT NULL,
    user_id        uuid        NOT NULL,

    question       text        NOT NULL,
    question_type  varchar(20) NOT NULL,
    explanation    text                 DEFAULT NULL,
    -- Correct boolean answer (for true or false questions)
    is_true_answer boolean              DEFAULT NULL,
    -- Choices for multiple-choice quizzes
    choices        text[]               DEFAULT NULL,
    -- Correct answer indices (for multiple-choice questions)
    answer_indices int[]                DEFAULT NULL,
    -- Correct text answer (for short answer and essay questions)
    text_answer    text                 DEFAULT NULL,
    display_order  int                  DEFAULT 0,
    points         float                DEFAULT 10.0,

    created_at     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at     timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_question_type CHECK (question_type IN
                                        ('true_or_false', 'multiple_choice',
                                         'short_answer', 'essay', 'custom')),

    FOREIGN KEY (exam_id) REFERENCES app.exams (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_exam_items_exam_display_order
    ON app.exam_items (exam_id, display_order);

CREATE TABLE IF NOT EXISTS app.exam_responses
(
    id               uuid PRIMARY KEY,
    exam_id          uuid      NOT NULL,
    question_id      uuid      NOT NULL,
    user_id          uuid      NOT NULL,

    is_correct       boolean            DEFAULT FALSE,
    -- For true or false questions
    selected_bool    boolean            DEFAULT NULL,
    -- For multiple-choice quizzes
    selected_indices int[]              DEFAULT NULL,
    -- For short answer and essay questions
    text_answer      text               DEFAULT NULL,
    score            float              DEFAULT 0,

    created_at       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at       timestamp,

    FOREIGN KEY (exam_id) REFERENCES app.exams (id),
    FOREIGN KEY (question_id) REFERENCES app.exam_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_exam_responses_exam
    ON app.exam_responses (exam_id);

CREATE TABLE IF NOT EXISTS app.exam_results
(
    id         uuid PRIMARY KEY,
    exam_id    uuid      NOT NULL,
    user_id    uuid      NOT NULL,

    score      float     NOT NULL,
    max_score  float     NOT NULL,
    feedback   text               DEFAULT NULL,

    start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp,

    FOREIGN KEY (exam_id) REFERENCES app.exams (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_exam_results_exam
    ON app.exam_results (exam_id);

-- TODO(mj): common reports for exams and quizzes
CREATE TABLE IF NOT EXISTS app.exam_question_reports
(
    id            uuid PRIMARY KEY,
    exam_id       uuid      NOT NULL,
    question_id   uuid      NOT NULL,
    user_id       uuid      NOT NULL,

    report_reason text      NOT NULL,

    created_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    timestamp,

    FOREIGN KEY (exam_id) REFERENCES app.exams (id),
    FOREIGN KEY (question_id) REFERENCES app.exam_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_exam_question_reports_created_at
    ON app.exam_question_reports (created_at);

CREATE TABLE IF NOT EXISTS app.lectures
(
    id                uuid PRIMARY KEY,
    course_id         uuid         NOT NULL,
    user_id           uuid         NOT NULL,

    title             varchar(255) NOT NULL,
    material_path     varchar(255) NOT NULL,
    material_type     varchar(20)  NOT NULL, -- e.g., 'pdf', 'pptx'
    display_order_lex varchar(255) NOT NULL, -- NOTE(mj): lexorank.

    note              jsonb                 DEFAULT NULL,
    summary           jsonb                 DEFAULT NULL,
    summary_status    varchar(20)  NOT NULL DEFAULT 'not_started',

    created_at        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_summary_status CHECK (summary_status IN
                                         ('not_started', 'in_progress', 'completed')),

    FOREIGN KEY (course_id) REFERENCES app.courses (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_lectures_course_created_at
    ON app.lectures (course_id, created_at);
CREATE INDEX IF NOT EXISTS idx_lectures_course_updated_at
    ON app.lectures (course_id, updated_at);
CREATE INDEX IF NOT EXISTS idx_lectures_course_display_order_lex
    ON app.lectures (course_id, display_order_lex);

CREATE TABLE IF NOT EXISTS app.quizzes
(
    id                  uuid PRIMARY KEY,
    lecture_id          uuid         NOT NULL,
    user_id             uuid         NOT NULL,

    title               varchar(255) NOT NULL,
    status              varchar(20)  NOT NULL,
    referenced_lectures uuid[],

    created_at          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at          timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_quiz_status CHECK (status IN ('not_started', 'submitted', 'graded')),

    FOREIGN KEY (lecture_id) REFERENCES app.lectures (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_quizzes_lecture_created_at
    ON app.quizzes (lecture_id, created_at);
CREATE INDEX IF NOT EXISTS idx_quizzes_lecture_updated_at
    ON app.quizzes (lecture_id, updated_at);

CREATE TABLE IF NOT EXISTS app.quiz_items
(
    id             uuid PRIMARY KEY,
    quiz_id        uuid        NOT NULL,
    user_id        uuid        NOT NULL,

    question       text        NOT NULL,
    question_type  varchar(20) NOT NULL,
    explanation    text                 DEFAULT NULL,
    -- Correct boolean answer (for true or false questions)
    is_true_answer boolean              DEFAULT NULL,
    -- Choices for multiple-choice quizzes
    choices        text[]               DEFAULT NULL,
    -- Correct answer indices (for multiple-choice questions)
    answer_indices int[]                DEFAULT NULL,
    -- Correct text answer (for short answer and essay questions)
    text_answer    text                 DEFAULT NULL,
    display_order  int                  DEFAULT 0,
    points         float                DEFAULT 10.0,

    created_at     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at     timestamp,

    -- NOTE(mj): move constraints check to the application layer?
    CONSTRAINT chk_question_type CHECK (question_type IN
                                        ('true_or_false', 'multiple_choice',
                                         'short_answer', 'essay', 'custom')),

    FOREIGN KEY (quiz_id) REFERENCES app.quizzes (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_items_quiz_display_order
    ON app.quiz_items (quiz_id, display_order);

CREATE TABLE IF NOT EXISTS app.quiz_responses
(
    id               uuid PRIMARY KEY,
    quiz_id          uuid      NOT NULL,
    question_id      uuid      NOT NULL,
    user_id          uuid      NOT NULL,

    is_correct       boolean            DEFAULT FALSE,
    -- For true or false questions
    selected_bool    boolean            DEFAULT NULL,
    -- For multiple-choice quizzes
    selected_indices int[]              DEFAULT NULL,
    -- For short answer and essay questions
    text_answer      text               DEFAULT NULL,
    score            float              DEFAULT 0,

    created_at       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at       timestamp,

    FOREIGN KEY (quiz_id) REFERENCES app.quizzes (id),
    FOREIGN KEY (question_id) REFERENCES app.quiz_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_responses_quiz
    ON app.quiz_responses (quiz_id);

CREATE TABLE IF NOT EXISTS app.quiz_results
(
    id         uuid PRIMARY KEY,
    quiz_id    uuid      NOT NULL,
    user_id    uuid      NOT NULL,

    score      float     NOT NULL,
    max_score  float     NOT NULL,
    feedback   text               DEFAULT NULL,

    start_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp,

    FOREIGN KEY (quiz_id) REFERENCES app.quizzes (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_results_quiz
    ON app.quiz_results (quiz_id);

-- TODO(mj): common reports for exams and quizzes
CREATE TABLE IF NOT EXISTS app.quiz_question_reports
(
    id            uuid PRIMARY KEY,
    quiz_id       uuid      NOT NULL,
    question_id   uuid      NOT NULL,
    user_id       uuid      NOT NULL,

    report_reason text      NOT NULL,

    created_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at    timestamp,

    FOREIGN KEY (quiz_id) REFERENCES app.quizzes (id),
    FOREIGN KEY (question_id) REFERENCES app.quiz_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_quiz_question_reports_created_at
    ON app.quiz_question_reports (created_at);

CREATE TABLE IF NOT EXISTS app.liked_quiz_items
(
    id          uuid PRIMARY KEY,
    quiz_id     uuid      NOT NULL,
    question_id uuid      NOT NULL,
    user_id     uuid      NOT NULL,

    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (quiz_id) REFERENCES app.quizzes (id),
    FOREIGN KEY (question_id) REFERENCES app.quiz_items (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_liked_quiz_items_quiz_created_at
    ON app.liked_quiz_items (quiz_id, created_at);

CREATE TABLE IF NOT EXISTS app.qna_chat
(
    id         uuid PRIMARY KEY,
    lecture_id uuid      NOT NULL,
    user_id    uuid      NOT NULL,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp,

    FOREIGN KEY (lecture_id) REFERENCES app.lectures (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_qna_chat_lecture
    ON app.qna_chat (lecture_id);

CREATE TABLE IF NOT EXISTS app.qna_chat_messages
(
    id          uuid PRIMARY KEY,
    qna_chat_id uuid      NOT NULL,
    user_id     uuid      NOT NULL,

    message     text      NOT NULL,
    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  timestamp,

    FOREIGN KEY (qna_chat_id) REFERENCES app.qna_chat (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_qna_chat_messages_qna_chat_created_at
    ON app.qna_chat_messages (qna_chat_id, created_at);

CREATE TABLE IF NOT EXISTS app.liked_qna_answers
(
    id          uuid PRIMARY KEY,
    qna_chat_id uuid      NOT NULL,
    message_id  uuid      NOT NULL,
    user_id     uuid      NOT NULL,

    created_at  timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (qna_chat_id) REFERENCES app.qna_chat (id),
    FOREIGN KEY (message_id) REFERENCES app.qna_chat_messages (id),
    FOREIGN KEY (user_id) REFERENCES app.users (id)
);

CREATE INDEX IF NOT EXISTS idx_liked_qna_answers_qna_chat_created_at
    ON app.liked_qna_answers (qna_chat_id, created_at);

--
-- The following tables are for the school calendar.
--

CREATE TABLE IF NOT EXISTS app.school
(
    id         uuid PRIMARY KEY,
    name       varchar(100) NOT NULL,

    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_school_name ON app.school (name);

CREATE TABLE IF NOT EXISTS app.school_calendars
(
    id         uuid PRIMARY KEY,
    school_id  uuid      NOT NULL,

    year       int       NOT NULL,

    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at timestamp,

    FOREIGN KEY (school_id) REFERENCES app.school (id)
);

CREATE INDEX IF NOT EXISTS idx_school_calendars_school_yea_season
    ON app.school_calendars (school_id, year);
