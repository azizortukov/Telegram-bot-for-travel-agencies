package uz.anas.star_tour;

import com.pengrad.telegrambot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
@EnableAsync
@EnableMongoAuditing
public class StarTourTgBotSpringBootApplication {

    @Value("${telegram.bot.token}")
    private String token;

    public static void main(String[] args) {
        SpringApplication.run(StarTourTgBotSpringBootApplication.class, args);
    }

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(token);
    }
}
