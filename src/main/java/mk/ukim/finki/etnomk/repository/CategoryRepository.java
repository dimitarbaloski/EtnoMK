package mk.ukim.finki.etnomk.repository;

import mk.ukim.finki.etnomk.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}