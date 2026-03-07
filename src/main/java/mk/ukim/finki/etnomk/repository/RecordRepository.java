package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findByTitleContainingIgnoreCase(String keyword);

    List<Record> findByRegion_Name(String region);

    List<Record> findByCategory_Name(String category);

    List<Record> findByRegion_NameAndCategory_Name(String region, String category);

}