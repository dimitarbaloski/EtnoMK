package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Record;
import mk.ukim.finki.etnomk.service.RecordService;

import java.util.List;
import java.util.Optional;

public class RecordServiceImpl implements RecordService {
    @Override
    public List<Record> findAll() {
        return List.of();
    }

    @Override
    public Optional<Record> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public Record createRecord(Record record) {
        return null;
    }

    @Override
    public Record updateRecord(Long id, Record updatedRecord) {
        return null;
    }

    @Override
    public void deleteRecord(Long id) {

    }

    @Override
    public List<Record> searchRecords(String keyword) {
        return List.of();
    }
}
