package uz.anas.star_tour.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import uz.anas.star_tour.entity.enums.Role;
import uz.anas.star_tour.entity.enums.TelegramState;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document
public class TelegramUser {

    private String id;
    private Long chatId;
    private String phoneNumber;
    private TelegramState state;
    private Role role;
    private Tour chosenTour;
    private Category chosenCategory;
    private List<Integer> messageId;
    private List<Integer> scheduledMessageId;
    private String chosenAppId;

    public String getFiringDetails() {
        return "Telefon raqami " + phoneNumber + " bo'lgan operatorlar ro'yxatidan chiqarildi";
    }

    public boolean checkForSuperAdmin() {
        return false;
    }

    public String getAcceptEditorDetails() {
        return "Telefon raqami " + phoneNumber + " bo'lgan operator unvoni Editor ga o'zgartirildi";
    }

    public String getAcceptOperatorDetails() {
        return "Telefon raqami " + phoneNumber + " bo'lgan Editor unvoni operator ga o'zgartirildi";
    }

}
