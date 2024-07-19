package uz.anas.star_tour.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import uz.anas.star_tour.entity.TelegramUser;

import java.util.Optional;

public interface UserRepo extends MongoRepository<TelegramUser, String> {

    Optional<TelegramUser> findByChatId(Long chatId);

    void deleteByChatId(Long chatId);
}
