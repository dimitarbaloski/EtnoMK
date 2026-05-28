package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByRecord_RecordId(Long recordId);

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

    /**
     * Same nearest-neighbour search, but restricted to records from one region.
     * Used when the query record/flow knows the ethnographic region and should
     * not mix costumes from other regions into the similarity result.
     */
    @Query(value = """
            SELECT i.*
            FROM   images i
            JOIN   records r ON r.record_id = i.record_id
            WHERE  i.embedding IS NOT NULL
              AND  i.record_id != :recordId
              AND  r.region_id = :regionId
              AND  i.embedding <=> CAST(:embedding AS vector) <= :maxDistance
            ORDER  BY i.embedding <=> CAST(:embedding AS vector)
            LIMIT  :limit
            """, nativeQuery = true)
    List<Image> findSimilarInRegion(
            @Param("recordId") Long recordId,
            @Param("regionId") Long regionId,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);
}
