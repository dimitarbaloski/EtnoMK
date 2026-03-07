package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}