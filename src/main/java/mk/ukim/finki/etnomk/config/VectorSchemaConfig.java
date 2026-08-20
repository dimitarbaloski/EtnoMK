package mk.ukim.finki.etnomk.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class VectorSchemaConfig implements ApplicationRunner {

    private static final String VECTOR_DIMENSION = "1536";
    private final JdbcTemplate jdbcTemplate;

    public VectorSchemaConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        normalizeImagesEmbeddingColumn();
        normalizePatchEmbeddingColumn();
    }

    private void normalizeImagesEmbeddingColumn() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'images'
                          AND column_name = 'embedding'
                    ) AND EXISTS (
                        SELECT 1
                        FROM pg_attribute a
                        JOIN pg_class c ON c.oid = a.attrelid
                        JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = 'public'
                          AND c.relname = 'images'
                          AND a.attname = 'embedding'
                          AND format_type(a.atttypid, a.atttypmod) <> 'vector(%s)'
                    ) THEN
                        ALTER TABLE public.images
                            ALTER COLUMN embedding TYPE vector(%s)
                            USING NULL::vector(%s);
                    END IF;
                END $$;
                """.formatted(VECTOR_DIMENSION, VECTOR_DIMENSION, VECTOR_DIMENSION));
    }

    private void normalizePatchEmbeddingColumn() {
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name = 'image_pattern_patches'
                    ) THEN
                        IF NOT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'image_pattern_patches'
                              AND column_name = 'embedding'
                        ) THEN
                            ALTER TABLE public.image_pattern_patches
                                ADD COLUMN embedding vector(%s);
                        ELSIF EXISTS (
                            SELECT 1
                            FROM pg_attribute a
                            JOIN pg_class c ON c.oid = a.attrelid
                            JOIN pg_namespace n ON n.oid = c.relnamespace
                            WHERE n.nspname = 'public'
                              AND c.relname = 'image_pattern_patches'
                              AND a.attname = 'embedding'
                              AND format_type(a.atttypid, a.atttypmod) <> 'vector(%s)'
                        ) THEN
                            ALTER TABLE public.image_pattern_patches
                                ALTER COLUMN embedding TYPE vector(%s)
                                USING NULL::vector(%s);
                        END IF;
                    END IF;
                END $$;
                """.formatted(VECTOR_DIMENSION, VECTOR_DIMENSION, VECTOR_DIMENSION, VECTOR_DIMENSION));
    }
}
