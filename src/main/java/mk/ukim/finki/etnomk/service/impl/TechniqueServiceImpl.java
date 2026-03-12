package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Technique;
import mk.ukim.finki.etnomk.repository.TechniqueRepository;
import mk.ukim.finki.etnomk.service.TechniqueService;

import java.util.List;

public class TechniqueServiceImpl implements TechniqueService {

    private final TechniqueRepository techniqueRepository;

    public TechniqueServiceImpl(TechniqueRepository techniqueRepository) {
        this.techniqueRepository = techniqueRepository;
    }

    @Override
    public List<Technique> findAll() {
        return techniqueRepository.findAll();
    }
}
