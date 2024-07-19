package uz.anas.star_tour.bot;

import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.service.TelegramUserService;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.enums.TelegramState;
import uz.anas.star_tour.bot_service.AdminBotService;
import uz.anas.star_tour.bot_service.BotService;
import uz.anas.star_tour.bot_service.LogService;

@Service
@RequiredArgsConstructor
public class MyBot {

    private final TelegramUserService telegramUserService;
    private final BotService botService;
    private final AdminBotService adminBotService;
    private final LogService logService;

    @Async
    public void handleUpdate(Update update) {
        if (update.message() != null) {
            Message message = update.message();
            Long chatId = message.chat().id();
            TelegramUser currentUser = telegramUserService.checkUser(chatId);
            switch (currentUser.getRole()) {
                case SUPPORT -> handleSupportRequest(message, currentUser);
                case CLIENT -> handleClientRequest(message, currentUser);
                case EDITOR -> handleEditorRequest(message, currentUser);
                case ADMIN -> handleAdminRequest(message, currentUser);
            }
        } else if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            Long chatId = callbackQuery.from().id();
            TelegramUser currentUser = telegramUserService.checkUser(chatId);

            switch (currentUser.getRole()) {
                case CLIENT -> botService.sendMessageToOpAndShowCategories(currentUser, callbackQuery.data());
                case EDITOR -> botService.checkInsideCategoriesAndTakeAction(currentUser, callbackQuery.data());
                case ADMIN -> adminBotService.checkInsideCategoriesAndTakeAction(currentUser, callbackQuery.data());
                case SUPPORT -> botService.changeAppStatus(currentUser, callbackQuery);

            }
        }
    }

    private void handleAdminRequest(Message message, TelegramUser currentUser) {
        if (message.text() != null) {
            String textMessage = message.text();
            if (textMessage.equals("/file")) {
                adminBotService.createReport(currentUser);
                return;
            }
            switch (currentUser.getState()) {
                case START -> adminBotService.sendCategoriesAdmin(currentUser);
                case CATEGORIES -> adminBotService.checkCategoriesAndTakeAction(currentUser, textMessage);
                case TOUR, ADD_OPERATOR, ADD_EDITOR -> adminBotService.checkForBack(currentUser, textMessage);
                case ADD_CATEGORY -> botService.getNewCategoryAndSaveIt(currentUser, textMessage, true);
                case ADD_TOUR -> botService.getNewTourAndSaveIt(currentUser, textMessage);
                case REM_CATEGORY -> botService.removeCategory(currentUser, textMessage, true);
                case CHANGE_TOUR -> botService.changeTourAndSaveIt(currentUser, textMessage, true);
                case EDIT_CATEGORY -> botService.editCategory(currentUser, textMessage, true);
                case OPERATOR -> botService.addAdminOrBack(currentUser, textMessage, true);
                case EDIT_CHOSEN_CATEGORY -> botService.editCategoryAndSendBack(currentUser, textMessage, true);
                case EDITORS -> adminBotService.addEditorOrBack(currentUser, textMessage);
                case SHOW_APPLICATION -> adminBotService.checkApplication(currentUser, textMessage, message);
            }
        }
    }

    private void handleEditorRequest(Message message, TelegramUser currentUser) {
        if (message.text() != null) {
            String textMessage = message.text();

            switch (currentUser.getState()) {
                case START -> botService.sendCategoriesEditor(currentUser);
                case CATEGORIES -> botService.checkCategoriesAndTakeAction(currentUser, textMessage);
                case TOUR, ADD_OPERATOR -> botService.checkForBack(currentUser, textMessage);
                case ADD_CATEGORY -> botService.getNewCategoryAndSaveIt(currentUser, textMessage, false);
                case ADD_TOUR -> botService.getNewTourAndSaveIt(currentUser, textMessage);
                case REM_CATEGORY -> botService.removeCategory(currentUser, textMessage, false);
                case CHANGE_TOUR -> botService.changeTourAndSaveIt(currentUser, textMessage, false);
                case EDIT_CATEGORY -> botService.editCategory(currentUser, textMessage, false);
                case OPERATOR -> botService.addAdminOrBack(currentUser, textMessage, false);
                case EDIT_CHOSEN_CATEGORY -> botService.editCategoryAndSendBack(currentUser, textMessage, false);
            }
        }
    }

    private void handleClientRequest(Message message, TelegramUser currentUser) {
        if (message.text() != null) {
            String textMessage = message.text();
            if (currentUser.getState().equals(TelegramState.START)) {
                botService.sendWelcomeMsgAskContact(currentUser);
            } else if (currentUser.getState().equals(TelegramState.SHARE_CONTACT)) {
                botService.sendWelcomeMsgAskContact(currentUser);
            } else if (currentUser.getState().equals(TelegramState.CATEGORIES)) {
                botService.sendTourInfo(currentUser, textMessage, message);
            } else if (currentUser.getState().equals(TelegramState.TOUR)) {
                botService.sendCategoriesClient(currentUser, message);
            }

        } else if (message.contact() != null && currentUser.getState().equals(TelegramState.SHARE_CONTACT)) {
            Contact contact = message.contact();
            String phone = contact.phoneNumber();
            currentUser.setPhoneNumber(phone.contains("+") ? phone : "+" + phone);
            if (currentUser.checkForSuperAdmin()) {
                logService.dataLog("Super admin recognized: " + currentUser.getPhoneNumber());
                adminBotService.sendCategoriesAdmin(currentUser);
            } else {
                logService.dataLog("New user logged: " + currentUser.getPhoneNumber());
                botService.sendCategoriesClient(currentUser, message);
            }
        }
    }

    private void handleSupportRequest(Message message, TelegramUser currentUser) {
        if (message.text() != null) {
            String messageText = message.text();
            if (currentUser.getState().equals(TelegramState.SHARE_FEEDBACK)) {
                botService.saveFeedbackAndBack(currentUser, messageText);
            } else if (messageText.equals(BotConstant.IN_PROGRESS_SUP_BTN)) {
                botService.clearMessage(currentUser);
                botService.sendAllProgress(currentUser);
            } else if (messageText.equals(BotConstant.NO_CONNECTION_SUP_BTN)) {
                botService.clearMessage(currentUser);
                botService.sendAllNoConnection(currentUser);
            }
            botService.sendCategoriesSupport(currentUser);
        }
    }

}
