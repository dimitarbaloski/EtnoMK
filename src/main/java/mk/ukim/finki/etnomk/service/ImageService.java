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
}
