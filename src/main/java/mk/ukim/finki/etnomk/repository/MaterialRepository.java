package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}