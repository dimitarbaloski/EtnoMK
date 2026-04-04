package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialService {
    List<Material> findAll();
    Optional<Material> findById(Long id);
    Material createMaterial(Material material);
    void deleteMaterial(Long id);
}