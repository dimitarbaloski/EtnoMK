CREATE EXTENSION IF NOT EXISTS vector;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'images'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'images'
              AND column_name = 'embedding'
              AND udt_name <> 'vector'
        ) THEN
            ALTER TABLE public.images DROP COLUMN embedding;
            ALTER TABLE public.images ADD COLUMN embedding vector(2048);
        ELSIF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'images'
              AND column_name = 'embedding'
        ) THEN
            ALTER TABLE public.images ADD COLUMN embedding vector(2048);
        END IF;
    END IF;
END $$;
