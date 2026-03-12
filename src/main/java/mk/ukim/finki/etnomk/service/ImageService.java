package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {
    public Image saveImage(Image image);
    public List<Image> getImagesByRecord(Long recordId);
    Image uploadImage(MultipartFile file, Long recordId) throws IOException;
}
