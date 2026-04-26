package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Image;
import mk.ukim.finki.etnomk.model.Record;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    Image saveImage(Image image);
    List<Image> getImagesByRecord(Long recordId);
    Image uploadImage(MultipartFile file, Record record) throws IOException;
    float[] getEmbedding(MultipartFile file) throws IOException, InterruptedException;

    /** Find records similar to the primary image of the given record. */
    List<Record> findSimilarRecords(Long recordId, int limit);

    /**
     * Find records similar to an uploaded image (no record ID required).
     * Used by the "upload a pattern" flow.
     */
    List<Record> findSimilarByUpload(MultipartFile file, int limit) throws IOException, InterruptedException;
}