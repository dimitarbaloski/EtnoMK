package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Region;
import mk.ukim.finki.etnomk.repository.RegionRepository;
import mk.ukim.finki.etnomk.service.RegionService;

import java.util.List;

public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    public RegionServiceImpl(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @Override
    public List<Region> findAll() {
        return regionRepository.findAll();
    }
}
