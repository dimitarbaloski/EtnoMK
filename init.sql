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
            ALTER TABLE public.images ADD COLUMN embedding vector(1536);
        ELSIF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'images'
              AND column_name = 'embedding'
        ) THEN
            ALTER TABLE public.images ADD COLUMN embedding vector(1536);
        END IF;
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS public.image_pattern_patches (
    patch_id BIGSERIAL PRIMARY KEY,
    image_id BIGINT NOT NULL REFERENCES public.images(image_id) ON DELETE CASCADE,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    embedding vector(1536)
);

CREATE INDEX IF NOT EXISTS idx_image_pattern_patches_image_id
    ON public.image_pattern_patches(image_id);

