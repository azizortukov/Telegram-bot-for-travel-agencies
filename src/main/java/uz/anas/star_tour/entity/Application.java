package uz.anas.star_tour.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;
import uz.anas.star_tour.bot.BotConstant;
import uz.anas.star_tour.entity.enums.RequestStatus;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document
public class Application {

    private String id;
    private String phoneNumber;
    private RequestStatus status;
    private String title;
    private String description;
    private String operatorNumber;
    private String createdTime;
    private String doneTime;

    public String infoForSuperAdmin() {
        return """
                *So'rovnoma:* %s
                *Mijozning nomer telefoni:* %s
                *So'rovnomaning status:* %s
                *Operator nomer telefoni:* %s
                *So'rovnoma kelgan vaqt:* %s
                *So'rovnoma javob qilingan vaqt:* %s
                *Sababi:* %s
                """.formatted(titleText(), phoneNumber, statusText(), operatorNumberText(),
                createdTime, doneTimeText(), descriptionText());
    }

    private String descriptionText() {
        return Objects.requireNonNullElse(description, "Sabab yozilmagan");
    }

    private String doneTimeText() {
        return Objects.requireNonNullElse(doneTime, "Operator xali ulanmadi");
    }

    private String operatorNumberText() {
        return Objects.requireNonNullElse(operatorNumber, "Operator xali ulanmadi");
    }

    public String statusText() {
        switch (status) {
            case IN_PROGRESS -> {
                return BotConstant.IN_PROGRESS_SUP_BTN;
            }
            case NO_ANSWER_CONNECTION -> {
                return BotConstant.NO_CONNECTION_SUP_BTN;
            }
            case DONE_SUCCESS -> {
                return BotConstant.SUCCESS_SDM_BTN;
            }
            case DONE_REJECT -> {
                return BotConstant.DENIED_SDM_BTN;
            }
        }
        return "";
    }

    private String titleText() {
        return title.split("\\r?\\n")[0];
    }

    public String infoForSupport() {
        return """
                *Tur paket:* %s
                *So'rovnoma kelgan vaqt:* %s
                *Mijoz telefon raqami:* %s""".formatted(title, createdTime, phoneNumber);
    }
}
