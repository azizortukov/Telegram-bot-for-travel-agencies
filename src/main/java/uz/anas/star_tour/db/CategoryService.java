package uz.anas.star_tour.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.entity.Category;
import uz.anas.star_tour.repo.CategoryRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;

    public List<Category> findAll() {
        return categoryRepo.findAll();
    }

    public void save(Category newCategory) {
        categoryRepo.save(newCategory);
    }

    public Optional<Category> getCategoryById(String id) {
        return categoryRepo.findById(id);
    }

    public Optional<Category> getCategoryByName(String textMessage) {
        return categoryRepo.findByName(textMessage);
    }

    public void delete(Category categoryByName) {
        categoryRepo.delete(categoryByName);
    }
}
