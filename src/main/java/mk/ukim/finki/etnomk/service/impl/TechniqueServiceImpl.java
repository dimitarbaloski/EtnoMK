package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Technique;
import mk.ukim.finki.etnomk.repository.TechniqueRepository;
import mk.ukim.finki.etnomk.service.TechniqueService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TechniqueServiceImpl implements TechniqueService {

    private final TechniqueRepository techniqueRepository;

    public TechniqueServiceImpl(TechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;
    }

    @Override
    public List<Technique> findAll() {
        return techniqueRepository.findAll();
    }

    @Override
    public Optional<Technique> findById(Long id) {
        return techniqueRepository.findById(id);
    }

    @Override
    public Technique createTechnique(Technique technique) {
        return techniqueRepository.save(technique);
    }

    @Override
    public void deleteTechnique(Long id) {
        techniqueRepository.deleteById(id);
    }
}