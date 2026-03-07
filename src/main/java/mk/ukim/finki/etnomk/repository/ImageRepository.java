package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> findByRecord_RecordId(Long recordId);

}