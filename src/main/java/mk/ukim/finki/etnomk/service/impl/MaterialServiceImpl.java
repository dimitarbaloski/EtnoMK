package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Material;
import mk.ukim.finki.etnomk.repository.MaterialRepository;
import mk.ukim.finki.etnomk.service.MaterialService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public List<Material> findAll() {
        return materialRepository.findAll();
    }

    @Override
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public Material createMaterial(Material material) {
        return materialRepository.save(material);
    }

    @Override
    public void deleteMaterial(Long id) {
        materialRepository.deleteById(id);
    }
}