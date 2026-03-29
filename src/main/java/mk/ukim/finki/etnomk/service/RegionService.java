package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Region;

import java.util.List;
import java.util.Optional;

public interface RegionService {
    List<Region> findAll();
    Optional<Region> findById(Long id);
    Region createRegion(Region region);
    void deleteRegion(Long id);
}
