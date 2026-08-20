package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RecordService {
    public List<Record> findAll();
    public Page<Record> findAll(Pageable pageable);
    public Optional<Record> findById(Long id);
    public Record createRecord(Record record);
    public Record updateRecord(Long id, Record updatedRecord);
    public void deleteRecord(Long id);
    public List<Record> searchRecords(String keyword);
    public Page<Record> searchRecords(String keyword, Pageable pageable);
}
