package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Image;

import java.util.List;

public interface ImageService {
    public Image saveImage(Image image);
    public List<Image> getImagesByRecord(Long recordId);
}
