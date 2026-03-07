package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Technique;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechniqueRepository extends JpaRepository<Technique, Long> {
}