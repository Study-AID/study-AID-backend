ALTER TABLE app.lectures
    ALTER COLUMN parsed_text TYPE jsonb USING parsed_text::jsonb;