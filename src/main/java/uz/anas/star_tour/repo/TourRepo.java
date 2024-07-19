package uz.anas.star_tour.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import uz.anas.star_tour.entity.Tour;

import java.util.List;

public interface TourRepo extends MongoRepository<Tour, String> {

    List<Tour> findAllByCategoryId(String categoryId);
    void deleteAllByCategoryId(String categoryId);

}
