package uz.anas.star_tour.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import uz.anas.star_tour.entity.Application;


public interface ApplicationRepo extends MongoRepository<Application, String> {
}
