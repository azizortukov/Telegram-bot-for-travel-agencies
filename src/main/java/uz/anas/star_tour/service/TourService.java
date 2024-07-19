package uz.anas.star_tour.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.entity.Category;
import uz.anas.star_tour.entity.Tour;
import uz.anas.star_tour.repo.CategoryRepo;
import uz.anas.star_tour.repo.TourRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TourService {

    private final TourRepo tourRepo;
    private final CategoryRepo categoryRepo;

    public void removeToursByCategoryId(String id) {
        tourRepo.deleteAllByCategoryId(id);
    }

    public List<Tour> getTourByCategoryNameNull(String categoryName) {
        Optional<Category> category = categoryRepo.findByName(categoryName);
        if (category.isPresent()) {
            List<Tour> tours = tourRepo.findAllByCategoryId(category.get().getId());
            if (!tours.isEmpty()) {
                return tours;
            }
        }
        return null;
    }

    public List<Tour> getTourByCategoryName(String categoryName) {
        Optional<Category> category = categoryRepo.findByName(categoryName);
        if (category.isPresent()) {
            return tourRepo.findAllByCategoryId(category.get().getId());
        }
        return new ArrayList<>();
    }

    public Optional<Tour> getTourById(String tourId) {
        return tourRepo.findById(tourId);
    }

    public void save(@NonNull Tour tour) {
        tourRepo.save(tour);
    }

    public void delete(@NonNull Tour tour) {
        tourRepo.delete(tour);
    }
}
