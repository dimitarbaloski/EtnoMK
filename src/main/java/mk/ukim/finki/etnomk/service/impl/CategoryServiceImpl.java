package mk.ukim.finki.etnomk.service.impl;

import mk.ukim.finki.etnomk.model.Category;
import mk.ukim.finki.etnomk.repository.CategoryRepository;
import mk.ukim.finki.etnomk.service.CategoryService;

import java.util.List;

public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}
