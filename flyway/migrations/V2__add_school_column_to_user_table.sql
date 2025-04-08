ALTER TABLE users
    ADD COLUMN school_id UUID,
    ADD FOREIGN KEY (school_id) REFERENCES app.schools(id);