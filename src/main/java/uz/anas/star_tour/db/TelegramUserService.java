package uz.anas.star_tour.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.enums.Role;
import uz.anas.star_tour.entity.enums.TelegramState;
import uz.anas.star_tour.repo.UserRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final UserRepo userRepo;

    public TelegramUser checkUser(Long chatId) {
        Optional<TelegramUser> user = userRepo.findByChatId(chatId);
        return user.orElseGet(() ->
                userRepo.save(TelegramUser.builder()
                        .role(Role.CLIENT)
                        .state(TelegramState.START)
                        .chatId(chatId)
                        .messageId(new ArrayList<>())
                        .scheduledMessageId(new ArrayList<>())
                        .build()));
    }

    public void updateUser(TelegramUser newUserWithUpdates) {
        userRepo.save(newUserWithUpdates);
    }

    public void deleteUser(Long chatId) {
        userRepo.deleteByChatId(chatId);
    }

    public List<TelegramUser> getAllUsers() {
        return userRepo.findAll();
    }

    public List<TelegramUser> getOperators() {
        return getAllUsers().stream()
                .filter(telegramUser -> telegramUser.getRole().equals(Role.SUPPORT))
                .toList();
    }

    public List<TelegramUser> getClients() {
        return getAllUsers().stream()
                .filter(telegramUser -> telegramUser.getRole().equals(Role.CLIENT))
                .toList();
    }

    public List<TelegramUser> getEditors() {
        return getAllUsers().stream()
                .filter(telegramUser -> telegramUser.getRole().equals(Role.EDITOR))
                .toList();
    }
}
