package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.repository.RecordRepository;
import mk.ukim.finki.etnomk.service.RecordService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordServiceImpl implements RecordService {

    private final RecordRepository recordRepository;

    public RecordServiceImpl(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    @Override
    public List<Record> findAll() {
        return recordRepository.findAll();
    }

    @Override
    public Page<Record> findAll(Pageable pageable) {
        return recordRepository.findAll(pageable);
    }

    @Override
    public Optional<Record> findById(Long id) {
        return recordRepository.findById(id);
    }

    @Override
    public Record createRecord(Record record) {
        return recordRepository.save(record);
    }

    @Override
    public Record updateRecord(Long id, Record updatedRecord) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found with id: " + id));
        record.setTitle(updatedRecord.getTitle());
        record.setDescription(updatedRecord.getDescription());
        record.setRegion(updatedRecord.getRegion());
        record.setCategory(updatedRecord.getCategory());
        record.setMaterial(updatedRecord.getMaterial());
        record.setTechnique(updatedRecord.getTechnique());
        record.setDateCreated(updatedRecord.getDateCreated());

        return recordRepository.save(record);
    }

    @Override
    public void deleteRecord(Long id) {
        recordRepository.deleteById(id);
    }

    @Override
    public List<Record> searchRecords(String keyword) {
        return recordRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Override
    public Page<Record> searchRecords(String keyword, Pageable pageable) {
        return recordRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }
}
