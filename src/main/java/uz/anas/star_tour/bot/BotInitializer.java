package uz.anas.star_tour.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uz.anas.star_tour.service.LogService;

@Configuration
@RequiredArgsConstructor
public class BotInitializer {

    private final TelegramBot tgBot;
    private final MyBot myBot;

    @Bean
    public CommandLineRunner commandLineRunner (LogService logService) {
        return args -> tgBot.setUpdatesListener(updates -> {
            try {
                updates.forEach(myBot::handleUpdate);
            } catch (Exception e) {
                logService.exceptionLogger(e.getMessage());
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
