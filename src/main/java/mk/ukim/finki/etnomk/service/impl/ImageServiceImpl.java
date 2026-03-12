package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Image;
import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.repository.ImageRepository;
import mk.ukim.finki.etnomk.repository.RecordRepository;
import mk.ukim.finki.etnomk.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Service
public class ImageServiceImpl implements ImageService {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private static final String UPLOAD_DIR = "uploads/";

    private final ImageRepository imageRepository;
    private final RecordRepository recordRepository;

    public ImageServiceImpl(ImageRepository imageRepository,
                            RecordRepository recordRepository) {
        this.imageRepository = imageRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public List<Image> getImagesByRecord(Long recordId) {
        return imageRepository.findByRecord_RecordId(recordId);
    }

    private BufferedImage resizeImage(BufferedImage originalImage) {

        java.awt.Image tmp = originalImage.getScaledInstance(WIDTH, HEIGHT, java.awt.Image.SCALE_SMOOTH);

        BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resized.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return resized;
    }

    @Override
    public Image uploadImage(MultipartFile file, Long recordId) throws IOException {

        Record record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        BufferedImage resizedImage = resizeImage(originalImage);

        String filename = UUID.randomUUID() + ".jpg";

        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File outputFile = new File(UPLOAD_DIR + filename);

        ImageIO.write(resizedImage, "jpg", outputFile);

        Image image = new Image();
        image.setImagePath("/uploads/" + filename);
        image.setRecord(record);

        return imageRepository.save(image);
    }


}