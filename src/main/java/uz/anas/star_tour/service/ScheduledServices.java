package uz.anas.star_tour.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.db.ApplicationService;
import uz.anas.star_tour.db.TelegramUserService;
import uz.anas.star_tour.db.TourService;
import uz.anas.star_tour.entity.Application;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.Tour;
import uz.anas.star_tour.entity.enums.RequestStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EnableScheduling
@Service
@RequiredArgsConstructor
public class ScheduledServices {

    private final TelegramBot telegramBot;
    private final TelegramUserService telegramUserService;
    private final KeyboardService keyboardService;
    private final ApplicationService applicationService;
    private final TourService tourService;

    @Async
    public void sendTourToUsers(Tour tour) {
        for (TelegramUser telegramUser : telegramUserService.getClients()) {
            if (telegramUser.getChatId() == null || telegramUser.getPhoneNumber() == null) {
                continue;
            }
            SendMessage sendMessage = new SendMessage(
                    telegramUser.getChatId(), tour.getName()
            );
            sendMessage.replyMarkup(keyboardService.tourBtns(tour));
            try {
                Message execute = telegramBot.execute(sendMessage).message();
                telegramUser.getMessageId().add(execute.messageId());
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
            telegramUserService.updateUser(telegramUser);
        }
    }

    @Async
    public void saveApplicationAndShowCategories(TelegramUser currentUser, String data) {
        List<TelegramUser> operators = telegramUserService.getOperators();
        if (operators == null) {
            return;
        }
        Optional<Tour> tourOptional = tourService.getTourById(data);
        tourOptional.ifPresent(tour -> createAndSaveApplication(tour, currentUser));
    }

    @Async
    public void createAndSaveApplication(Tour tour, TelegramUser currentUser) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Application application = new Application(
                UUID.randomUUID().toString(),
                currentUser.getPhoneNumber(),
                RequestStatus.IN_PROGRESS,
                tour.getName().split("\\r?\\n")[0],
                null,
                null,
                LocalDateTime.now().format(formatter),
                null
        );
        applicationService.createApplication(application);
    }
}
