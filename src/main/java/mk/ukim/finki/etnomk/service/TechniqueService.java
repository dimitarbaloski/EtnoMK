package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Technique;

import java.util.List;
import java.util.Optional;

public interface TechniqueService {
    List<Technique> findAll();
    Optional<Technique> findById(Long id);
}
