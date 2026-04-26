package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByRecord_RecordId(Long recordId);
    List<Image> findByEmbeddingIsNull();

    /**
     * Cosine-distance nearest-neighbour search via pgvector (<=> operator).
     * Excludes images belonging to the given recordId so a record is never
     * "similar to itself".  Pass recordId = -1 when searching by uploaded image.
     */
    @Query(value = """
            SELECT i.*
            FROM   images i
            WHERE  i.embedding IS NOT NULL
              AND  i.record_id != :recordId
              AND  i.embedding <=> CAST(:embedding AS vector) <= :maxDistance
            ORDER  BY i.embedding <=> CAST(:embedding AS vector)
            LIMIT  :limit
            """, nativeQuery = true)
    List<Image> findSimilar(
            @Param("recordId") Long recordId,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);
}
