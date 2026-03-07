package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Record;

import java.util.List;
import java.util.Optional;

public interface RecordService {
    public List<Record> findAll();
    public Optional<Record> findById(Long id);
    public Record createRecord(Record record);
    public Record updateRecord(Long id, Record updatedRecord);
    public void deleteRecord(Long id);
    public List<Record> searchRecords(String keyword);
}
