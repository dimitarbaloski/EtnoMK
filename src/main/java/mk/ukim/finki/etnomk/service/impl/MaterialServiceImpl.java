package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Material;
import mk.ukim.finki.etnomk.repository.MaterialRepository;
import mk.ukim.finki.etnomk.service.MaterialService;

import java.util.List;

public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public List<Material> findAll() {
        return materialRepository.findAll();
    }
}
