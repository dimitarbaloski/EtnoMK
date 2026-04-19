package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Region;
import mk.ukim.finki.etnomk.repository.RegionRepository;
import mk.ukim.finki.etnomk.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    public RegionServiceImpl(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public List<Region> findAll() {
        return regionRepository.findAll();
    }

    @Override
    public Optional<Region> findById(Long id) {
        return regionRepository.findById(id);
    }

    @Override
    public Region createRegion(Region region) {
        return regionRepository.save(region);
    }

    @Override
    public void deleteRegion(Long id) {
        regionRepository.deleteById(id);
    }
}
