package uz.anas.star_tour.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.entity.Application;
import uz.anas.star_tour.entity.enums.RequestStatus;
import uz.anas.star_tour.repo.ApplicationRepo;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepo applicationRepo;

    public void createApplication(Application newApplication) {
        applicationRepo.save(newApplication);
    }

    public Application getApplicationById(String id) {
        return applicationRepo.findById(id).orElse(null);
    }

    public void updateApplication(Application newApp) {
        applicationRepo.save(newApp);
    }

    public List<Application> getAllApplication() {
        return applicationRepo.findAll();
    }

    public List<Application> getAllProgressApps() {
        return getAllApplication().stream()
                .filter(application -> application.getStatus().equals(RequestStatus.IN_PROGRESS))
                .toList();
    }
    public List<Application> getAllNoConnectionApps() {
        return getAllApplication().stream()
                .filter(application -> application.getStatus().equals(RequestStatus.NO_ANSWER_CONNECTION))
                .toList();
    }

    public List<Application> getApplicationByStatus(RequestStatus requestStatus) {
        return getAllApplication().stream()
                .filter(application -> application.getStatus().equals(requestStatus))
                .toList();
    }
}
