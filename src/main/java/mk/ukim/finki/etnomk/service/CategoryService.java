package mk.ukim.finki.etnomk.service;

import mk.ukim.finki.etnomk.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    Category createCategory(Category category);
    void deleteCategory(Long id);
}
