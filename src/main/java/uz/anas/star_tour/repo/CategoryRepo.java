package uz.anas.star_tour.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import uz.anas.star_tour.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepo extends MongoRepository<Category, String> {

    Optional<Category> findByName(String categoryName);

    List<Category> findALlByOrderByCreatedAt();
}
