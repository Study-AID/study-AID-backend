-- V6__add_dummy_data_for_test.sql

-- 학기는 a, 과목은 b, 강의는 c로 시작하는 UUID를 사용하여 데이터 삽입
-- string1 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT 'a1111111-1111-1111-1111-111111111111', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = 'a1111111-1111-1111-1111-111111111111'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT 'b1111111-1111-1111-1111-111111111111', 'a1111111-1111-1111-1111-111111111111', u.id, '자료구조', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = 'b1111111-1111-1111-1111-111111111111'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT 'c1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111111', u.id,
       '자료구조 01 - 소개', '/storage/lectures/ds_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = 'c1111111-1111-1111-1111-111111111111'
);

-- string2 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT 'a2222222-2222-2222-2222-222222222222', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = 'a2222222-2222-2222-2222-222222222222'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT 'b2222222-2222-2222-2222-222222222222', 'a2222222-2222-2222-2222-222222222222', u.id, '알고리즘', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = 'b2222222-2222-2222-2222-222222222222'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT 'c2222222-2222-2222-2222-222222222222', 'b2222222-2222-2222-2222-222222222222', u.id,
       '알고리즘 01 - 소개', '/storage/lectures/algo_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = 'c2222222-2222-2222-2222-222222222222'
);

-- string3 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT 'a3333333-3333-3333-3333-333333333333', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = 'a3333333-3333-3333-3333-333333333333'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT 'b3333333-3333-3333-3333-333333333333', 'a3333333-3333-3333-3333-333333333333', u.id, '운영체제', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = 'b3333333-3333-3333-3333-333333333333'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT 'c3333333-3333-3333-3333-333333333333', 'b3333333-3333-3333-3333-333333333333', u.id,
       '운영체제 01 - 소개', '/storage/lectures/os_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = 'c3333333-3333-3333-3333-333333333333'
);
