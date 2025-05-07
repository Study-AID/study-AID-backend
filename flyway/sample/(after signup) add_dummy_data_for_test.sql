-- postman으로 이메일: string1@, 이름: string 양식으로 3명 회원가입 후 진행
-- 학기는 a, 과목은 b, 강의는 c로 이루어진 UUID, 접두사는 유저 번호(string1이면 1)
-- string1 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT '1aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = '1aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT '1bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '1aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '자료구조', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = '1bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string1'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT '1ccccccc-cccc-cccc-cccc-cccccccccccc', '1bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', u.id,
       '자료구조 01 - 소개', '/storage/lectures/ds_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = '1ccccccc-cccc-cccc-cccc-cccccccccccc'
);

-- string2 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT '2aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = '2aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT '2bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '알고리즘', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = '2bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string2'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT '2ccccccc-cccc-cccc-cccc-cccccccccccc', '2bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', u.id,
       '알고리즘 01 - 소개', '/storage/lectures/algo_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = '2ccccccc-cccc-cccc-cccc-cccccccccccc'
);

-- string3 유저에게 데이터 삽입
WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.semesters (id, user_id, name, year, season, created_at)
SELECT '3aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '2023-1학기', 2023, 'spring', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.semesters WHERE id = '3aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.courses (id, semester_id, user_id, name, created_at)
SELECT '3bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '3aaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', u.id, '운영체제', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.courses WHERE id = '3bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
);

WITH u AS (
    SELECT id FROM app.users WHERE name = 'string3'
)
INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, created_at)
SELECT '3ccccccc-cccc-cccc-cccc-cccccccccccc', '3bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', u.id,
       '운영체제 01 - 소개', '/storage/lectures/os_01.pdf', 'pdf', '0|1', 'not_started', NOW()
FROM u
WHERE NOT EXISTS (
    SELECT 1 FROM app.lectures WHERE id = '3ccccccc-cccc-cccc-cccc-cccccccccccc'
);
