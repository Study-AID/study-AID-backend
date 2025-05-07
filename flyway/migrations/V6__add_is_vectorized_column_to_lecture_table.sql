-- V6__add_is_vectorized_column_to_lecture_table.sql
ALTER TABLE app.lectures
    ADD COLUMN is_vectorized BOOLEAN DEFAULT FALSE NOT NULL;