package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.ImagePatternPatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImagePatternPatchRepository extends JpaRepository<ImagePatternPatch, Long> {

    boolean existsByImage_ImageId(Long imageId);

    void deleteByImage_ImageId(Long imageId);

    /**
     * Patch-level nearest-neighbour search. This is better for uploaded motif
     * fragments because it compares against small areas of stored costume
     * images instead of one whole-costume embedding.
     */
    @Query(value = """
            SELECT p.*
            FROM   image_pattern_patches p
            JOIN   images i ON i.image_id = p.image_id
            WHERE  p.embedding IS NOT NULL
              AND  i.record_id != :recordId
              AND  p.embedding <=> CAST(:embedding AS vector) <= :maxDistance
            ORDER  BY p.embedding <=> CAST(:embedding AS vector)
            LIMIT  :limit
            """, nativeQuery = true)
    List<ImagePatternPatch> findSimilarPatches(
            @Param("recordId") Long recordId,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);

    @Query(value = """
            SELECT p.*
            FROM   image_pattern_patches p
            JOIN   images i ON i.image_id = p.image_id
            JOIN   records r ON r.record_id = i.record_id
            WHERE  p.embedding IS NOT NULL
              AND  i.record_id != :recordId
              AND  r.region_id = :regionId
              AND  p.embedding <=> CAST(:embedding AS vector) <= :maxDistance
            ORDER  BY p.embedding <=> CAST(:embedding AS vector)
            LIMIT  :limit
            """, nativeQuery = true)
    List<ImagePatternPatch> findSimilarPatchesInRegion(
            @Param("recordId") Long recordId,
            @Param("regionId") Long regionId,
            @Param("embedding") String embedding,
            @Param("maxDistance") double maxDistance,
            @Param("limit") int limit);
}
