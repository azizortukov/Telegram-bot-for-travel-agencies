package uz.anas.star_tour.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.bot.BotConstant;
import uz.anas.star_tour.db.*;
import uz.anas.star_tour.entity.Application;
import uz.anas.star_tour.entity.Category;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.Tour;
import uz.anas.star_tour.entity.enums.RequestStatus;
import uz.anas.star_tour.entity.enums.Role;
import uz.anas.star_tour.entity.enums.TelegramState;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BotService {

    private final TelegramBot tgBot;
    private final TelegramUserService telegramUserService;
    private final KeyboardService keyboardService;
    private final ApplicationService applicationService;
    private final TourService tourService;
    private final CategoryService categoryService;
    private final LogService logService;
    private final ScheduledServices scheduledServices;
    private final AdminKeyboardService adminKeyboardService;

    @SneakyThrows
    public void sendWelcomeMsgAskContact(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(
                currentUser.getChatId(), BotConstant.WELCOME_MSG);
        sendMessage.replyMarkup(KeyboardService.contactBtn());
        currentUser.setState(TelegramState.SHARE_CONTACT);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(sendMessage);
    }

    @SneakyThrows
    public void sendCategoriesClient(TelegramUser currentUser, Message message) {
        if (message != null) {
            currentUser.getMessageId().add(message.messageId());
        }
        clearMessage(currentUser);
        SendMessage sendMessage = new SendMessage(
                currentUser.getChatId(), BotConstant.CATEGORY_MSG
        );
        sendMessage.replyMarkup(keyboardService.categoryBtns());
        currentUser.setState(TelegramState.CATEGORIES);
        tgBot.execute(sendMessage);
        SendResponse execute = tgBot.execute(sendMessage);
        currentUser.getMessageId().add(execute.message().messageId());
        telegramUserService.updateUser(currentUser);
    }

    public void clearMessage(TelegramUser currentUser) {
        for (Integer messageId : currentUser.getMessageId()) {
            try {
                DeleteMessage deleteMessage = new DeleteMessage(currentUser.getChatId(), messageId);
                tgBot.execute(deleteMessage);
            } catch (Exception e) {
                currentUser.getMessageId().clear();
                logService.exceptionLogger(logService.getStackTraceAsString(e));
            }
        }
        currentUser.getMessageId().clear();
        telegramUserService.updateUser(currentUser);
    }


    public void sendTourInfo(TelegramUser currentUser, String categoryName, Message message) {
        List<Tour> tours = tourService.getTourByCategoryName(categoryName);
        if (tours == null) {
            sendCategoriesClient(currentUser, message);
            return;
        } else if (tours.isEmpty()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "Uzur, hozircha bu shaharga " +
                                                                               "tur paket mavjud emas");
            try {
                Message execute = tgBot.execute(sendMessage).message();
                currentUser.getScheduledMessageId().add(execute.messageId());
                sendCategoriesClient(currentUser, message);
                return;
            } catch (Exception e) {
                logService.exceptionLogger(logService.getStackTraceAsString(e));
            }
        }
        if (message != null) {
            currentUser.getMessageId().add(message.messageId());
        }
        for (Tour tour : tours) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), tour.getName());
            sendMessage.replyMarkup(keyboardService.tourBtns(tour));
            try {
                Message execute = tgBot.execute(sendMessage).message();
                currentUser.getMessageId().add(execute.messageId());
            } catch (Exception e) {
                logService.exceptionLogger(logService.getStackTraceAsString(e));
            }
        }
        telegramUserService.updateUser(currentUser);
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.TOUR_MSG);
        sendMessage.replyMarkup(keyboardService.backBtn());
        currentUser.setState(TelegramState.TOUR);
        try {
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
        } catch (Exception e) {
            logService.exceptionLogger(logService.getStackTraceAsString(e));
        }
    }

    @SneakyThrows
    public void sendMessageToOpAndShowCategories(TelegramUser currentUser, String data) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.REACH_MSG);
        Message execute = tgBot.execute(sendMessage).message();
        currentUser.getScheduledMessageId().add(execute.messageId());
        sendCategoriesClient(currentUser, null);
        scheduledServices.saveApplicationAndShowCategories(currentUser, data);
    }

    @SneakyThrows
    public void sendCategoriesEditor(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(
                currentUser.getChatId(), BotConstant.CHOOSE_MSG_ADM
        );
        sendMessage.replyMarkup(keyboardService.categoryAdminBtns());
        Message execute = tgBot.execute(sendMessage).message();
        currentUser.getScheduledMessageId().add(execute.messageId());
        currentUser.setState(TelegramState.CATEGORIES);
        telegramUserService.updateUser(currentUser);
    }

    @SneakyThrows
    public void checkCategoriesAndTakeAction(TelegramUser currentUser, String textMessage) {
        List<Tour> tours = tourService.getTourByCategoryNameNull(textMessage);
        if (tours != null) {
            showToursToChange(currentUser, tours);
        } else if (categoryService.getCategoryByName(textMessage).isPresent()) {
            askTourForChosenCategory(currentUser, textMessage);
        } else if (textMessage.equals(BotConstant.ADD_CAT_BTN_ADM)) {
            addCategory(currentUser);
        } else if (textMessage.equals(BotConstant.REM_CAT_BTN_ADN)) {
            chooseCategoryToRemove(currentUser);
        } else if (textMessage.equals(BotConstant.ADD_ADM_BTN_ADM)) {
            showOperators(currentUser, false);
        } else if (textMessage.equals(BotConstant.CHANGE_CAT_BTN_ADM)) {
            chooseCategoryToChange(currentUser);
        } else {
            sendCategoriesEditor(currentUser);
        }
    }

    public void chooseCategoryToChange(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CHOOSE_CAT_SDM_BTN);
        sendMessage.replyMarkup(keyboardService.categoryBtns());
        currentUser.setState(TelegramState.EDIT_CATEGORY);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(sendMessage);
    }

    public void chooseCategoryToRemove(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.REM_TOUR_ADM_MSG);
        sendMessage.replyMarkup(keyboardService.categoryBtns());
        currentUser.setState(TelegramState.REM_CATEGORY);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(sendMessage);
    }

    public void addCategory(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.ADD_TOUR_ADM_MSG);
        sendMessage.replyMarkup(keyboardService.backBtn());
        currentUser.setState(TelegramState.ADD_CATEGORY);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(sendMessage);
    }

    public void showOperators(TelegramUser currentUser, boolean isAdmin) {
        if (telegramUserService.getOperators().isEmpty()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.NO_SUPPORT_ADM_MSG);
            sendMessage.replyMarkup(keyboardService.addAdminAndBackBtns());
            currentUser.setState(TelegramState.OPERATOR);
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
            return;
        }
        for (TelegramUser admin : telegramUserService.getOperators()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(),
                    "Operator nomer telefon: " + admin.getPhoneNumber());
            sendMessage.replyMarkup(keyboardService.removeSupport(admin.getChatId(), isAdmin));
            tgBot.execute(sendMessage);
        }
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.ADD_ADMIN_ADM_MSG);
        sendMessage.replyMarkup(keyboardService.addAdminAndBackBtns());
        currentUser.setState(TelegramState.OPERATOR);
        Message execute = tgBot.execute(sendMessage).message();
        currentUser.getMessageId().add(execute.messageId());
        telegramUserService.updateUser(currentUser);
    }

    public void showToursToChange(TelegramUser currentUser, List<Tour> tours) {
        SendMessage message = new SendMessage(currentUser.getChatId(), BotConstant.CHANGE_TOUR_MSG_ADM);
        message.replyMarkup(keyboardService.backBtn());
        for (Tour tour : tours) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), tour.getName());
            sendMessage.replyMarkup(keyboardService.tourAdminBtn(tour));
            tgBot.execute(sendMessage);
        }
        currentUser.setState(TelegramState.TOUR);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(message);
    }

    @SneakyThrows
    public void checkInsideCategoriesAndTakeAction(TelegramUser currentUser, String data) {
        if (data.equals(BotConstant.BACK_BTN)) {
            clearMessage(currentUser);
            sendCategoriesEditor(currentUser);
            return;
        }
        if (currentUser.getState().equals(TelegramState.TOUR)) {
            if (tourService.getTourById(data).isPresent()) {
                Optional<Tour> tour = tourService.getTourById(data);
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "♻️Tur muvvafaqiyatli o'chirildi");
                if (tour.isPresent()) {
                    tourService.delete(tour.get());
                    currentUser.setState(TelegramState.CATEGORIES);
                    currentUser.setChosenTour(tour.get());
                }
                telegramUserService.updateUser(currentUser);
                tgBot.execute(sendMessage);
            } else if (categoryService.getCategoryById(data).isPresent()) {
                Optional<Category> category = categoryService.getCategoryById(data);
                category.ifPresent(currentUser::setChosenCategory);
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.PAY_ATTENTION_ADM_MSG);
                currentUser.setState(TelegramState.ADD_TOUR);
                telegramUserService.updateUser(currentUser);
                tgBot.execute(sendMessage);
            }
        } else if (currentUser.getState().equals(TelegramState.OPERATOR)) {
            TelegramUser user = telegramUserService.checkUser(Long.valueOf(data));
            if (user != null) {
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), user.getFiringDetails());
                clearMessage(currentUser);
                logService.dataLog("Operator was removed: " + user.getPhoneNumber());
                tgBot.execute(sendMessage);
                telegramUserService.deleteUser(Long.valueOf(data));
            }
        } else if (currentUser.getState().equals(TelegramState.ADD_OPERATOR)) {
            TelegramUser user = telegramUserService.checkUser(Long.valueOf(data));
            user.setRole(Role.SUPPORT);
            user.setState(TelegramState.START);
            telegramUserService.updateUser(currentUser);
            logService.dataLog("Operator was added: " + user.getPhoneNumber());
            clearMessage(currentUser);
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_ADM_ADD_MSG);
            tgBot.execute(sendMessage);
            sendCategoriesEditor(currentUser);
        } else if (currentUser.getState().equals(TelegramState.ADD_TOUR)) {
            Optional<Tour> tour = tourService.getTourById(data);
            if (tour.isPresent()) {
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "✅ Mijozlarga bu tur haqida" +
                                                                                   "ma'lumot junatildi");
                tgBot.execute(sendMessage);
                scheduledServices.sendTourToUsers(tour.get());
            }
            sendCategoriesEditor(currentUser);
        }
    }

    @SneakyThrows
    public void getNewCategoryAndSaveIt(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            if (isAdmin) {
                sendCategoriesAdmin(currentUser);
            }else {
                sendCategoriesEditor(currentUser);
            }
            return;
        }
        Category newCategory = new Category(textMessage);
        categoryService.save(newCategory);
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_CAT_ADM_MSG);
        tgBot.execute(sendMessage);
        logService.dataLog("Category was added, Name: " + newCategory.getName() + " Id: " + newCategory.getId());
        if (isAdmin) {
            sendCategoriesAdmin(currentUser);
        }else {
            sendCategoriesEditor(currentUser);
        }
    }

    @SneakyThrows
    public void getNewTourAndSaveIt(TelegramUser currentUser, String textMessage) {
        if (currentUser.getChosenCategory() != null) {
            Tour tour = new Tour(currentUser.getChosenCategory().getId(), textMessage);
            tourService.save(tour);
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_TOUR_ADM_MSG);
            sendMessage.replyMarkup(keyboardService.sendNewTourToClients(tour));
            tgBot.execute(sendMessage);
        }
    }

    @SneakyThrows
    public void removeCategory(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        Optional<Category> category = categoryService.getCategoryByName(textMessage);
        if (category.isPresent()) {
            Category categoryByName = category.get();
            tourService.removeToursByCategoryId(categoryByName.getId());
            logService.dataLog("Category was removed, Name: " + categoryByName.getName()
                               + " Id: " + categoryByName.getId());
            categoryService.delete(categoryByName);
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(),
                    BotConstant.CNF_CAT_ADM_MSG);
            tgBot.execute(sendMessage);
        }
        if (isAdmin) {
            sendCategoriesAdmin(currentUser);
        } else {
            sendCategoriesEditor(currentUser);
        }
    }

    @SneakyThrows
    public void changeTourAndSaveIt(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            if (isAdmin) {
                sendCategoriesEditor(currentUser);
            } else {
                sendCategoriesAdmin(currentUser);
            }
            return;
        }
        Optional<Tour> tourByTourId = tourService.getTourById(currentUser.getChosenTour().getId());
        if (tourByTourId.isPresent()) {
            tourService.delete(tourByTourId.get());
            SendMessage sendMessage = new SendMessage(
                    currentUser.getChatId(), BotConstant.CNF_TOUR_ADM_MSG);
            tgBot.execute(sendMessage);
        }
        if (isAdmin) {
            sendCategoriesAdmin(currentUser);
        } else {
            sendCategoriesEditor(currentUser);
        }
    }

    public void checkForBack(TelegramUser currentUser, String textMessage) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            sendCategoriesEditor(currentUser);
        }
    }

    @SneakyThrows
    public void addAdminOrBack(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            if (isAdmin) {
                sendCategoriesAdmin(currentUser);
            } else {
                sendCategoriesEditor(currentUser);
            }
        } else {
            addingOperator(currentUser, textMessage);
        }
    }

    public void addingOperator(TelegramUser currentUser, String textMessage) {
        if (textMessage.equals(BotConstant.ADD_TOUR_ADM_BTN)) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "Operatorga qushmoqchi bugan " +
                                                                               "odamingiz start tugmasini bosib, kontakt junatish tugmasini bosgan bo'lishi kerak");
            sendMessage.replyMarkup(keyboardService.showUsers());
            currentUser.setState(TelegramState.ADD_OPERATOR);
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
        }
    }

    @SneakyThrows
    public void editCategory(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            if (isAdmin) {
                sendCategoriesAdmin(currentUser);
            }else {
                sendCategoriesEditor(currentUser);
            }
            return;
        }
        Optional<Category> needsToChange = categoryService.getCategoryByName(textMessage);
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.ADD_TOUR_ADM_MSG);
        sendMessage.replyMarkup(keyboardService.backBtn());
        tgBot.execute(sendMessage);
        needsToChange.ifPresent(currentUser::setChosenCategory);
        currentUser.setState(TelegramState.EDIT_CHOSEN_CATEGORY);
        telegramUserService.updateUser(currentUser);
    }

    @SneakyThrows
    public void editCategoryAndSendBack(TelegramUser currentUser, String textMessage, boolean isAdmin) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            if (isAdmin) {
                sendCategoriesAdmin(currentUser);
            } else {
                sendCategoriesEditor(currentUser);
            }
            return;
        }
        Optional<Category> categoryById = categoryService.getCategoryById(currentUser.getChosenCategory().getId());
        categoryById.ifPresent(category -> {
            category.setName(textMessage);
            categoryService.save(category);
        });
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_CAT_ADM_MSG);
        tgBot.execute(sendMessage);
        if (isAdmin) {
            sendCategoriesAdmin(currentUser);
        }else {
            sendCategoriesEditor(currentUser);
        }
    }

    public void sendCategoriesSupport(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CHOOSE_MSG_SUP);
        sendMessage.replyMarkup(keyboardService.supportBtns());
        try {
            tgBot.execute(sendMessage);
        } catch (Exception e) {
            logService.exceptionLogger(logService.getStackTraceAsString(e));
        }
    }

    public void sendAllProgress(TelegramUser currentUser) {
        applicationService.getAllProgressApps()
                .forEach(application -> {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), application.infoForSupport());
                    sendMessage.replyMarkup(keyboardService.changeProgressAppsStatus(application));
                    sendMessage.parseMode(ParseMode.Markdown);
                    try {
                        Message execute = tgBot.execute(sendMessage).message();
                        currentUser.getMessageId().add(execute.messageId());
                    } catch (Exception e) {
                        logService.exceptionLogger(logService.getStackTraceAsString(e));
                    }
                });
        telegramUserService.updateUser(currentUser);
    }

    public void sendAllNoConnection(TelegramUser currentUser) {
        applicationService.getAllNoConnectionApps()
                .forEach(application -> {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), application.infoForSupport());
                    sendMessage.parseMode(ParseMode.Markdown);
                    sendMessage.replyMarkup(keyboardService.changeNoConnectionAppsStatus(application));
                    try {
                        Message execute = tgBot.execute(sendMessage).message();
                        currentUser.getMessageId().add(execute.messageId());
                    } catch (Exception e) {
                        logService.exceptionLogger(logService.getStackTraceAsString(e));
                    }
                });
        telegramUserService.updateUser(currentUser);
    }

    public void changeAppStatus(TelegramUser currentUser, CallbackQuery query) {
        String option = query.data();
        String appId = query.data().split("/")[1];
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (option.contains(BotConstant.INT_DAT_SIP)) {
            Application applicationById = applicationService.getApplicationById(appId);
            if (applicationById != null) {
                applicationById.setStatus(RequestStatus.DONE_SUCCESS);
                applicationById.setDoneTime(LocalDateTime.now().format(formatter));
                applicationById.setOperatorNumber(currentUser.getPhoneNumber());
                applicationService.updateApplication(applicationById);
                sendConfOfAppChange(currentUser);
            }
        } else if (option.contains(BotConstant.NO_RES_DAT_SUP)) {
            Application applicationById = applicationService.getApplicationById(appId);
            if (applicationById != null) {
                applicationById.setStatus(RequestStatus.NO_ANSWER_CONNECTION);
                applicationById.setDoneTime(LocalDateTime.now().format(formatter));
                applicationById.setOperatorNumber(currentUser.getPhoneNumber());
                applicationService.updateApplication(applicationById);
                sendConfOfAppChange(currentUser);
            }
        } else if (option.contains(BotConstant.NOT_INT_DAT_SUP)) {
            Application applicationById = applicationService.getApplicationById(appId);
            if (applicationById != null) {
                clearMessage(currentUser);
                applicationById.setStatus(RequestStatus.DONE_REJECT);
                applicationById.setDoneTime(LocalDateTime.now().format(formatter));
                applicationById.setOperatorNumber(currentUser.getPhoneNumber());
                applicationService.updateApplication(applicationById);
                askReasonOfAppDecline(currentUser, appId);
            }
        }
    }

    private void askReasonOfAppDecline(TelegramUser currentUser, String appId) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "Iltimos, sababini yozib qoldiring");
        sendMessage.replyMarkup(new ReplyKeyboardRemove(true));
        currentUser.setChosenAppId(appId);
        currentUser.setState(TelegramState.SHARE_FEEDBACK);
        telegramUserService.updateUser(currentUser);
        try {
            tgBot.execute(sendMessage);
        } catch (Exception e) {
            logService.exceptionLogger(logService.getStackTraceAsString(e));
        }
    }

    private void sendConfOfAppChange(TelegramUser currentUser) {
        try {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "So'rovnomani statusi o'zgartirildi");
            tgBot.execute(sendMessage);
        } catch (Exception e) {
            logService.exceptionLogger(logService.getStackTraceAsString(e));
        }
    }

    public void saveFeedbackAndBack(TelegramUser currentUser, String messageText) {
        if (currentUser.getChosenAppId() != null) {
            Application applicationById = applicationService.getApplicationById(currentUser.getChosenAppId());
            if (applicationById != null) {
                applicationById.setDescription(messageText);
                applicationService.updateApplication(applicationById);
            }
        }
        currentUser.setState(TelegramState.CATEGORIES);
        telegramUserService.updateUser(currentUser);
        sendConfOfAppChange(currentUser);
    }

    public void askTourForChosenCategory(TelegramUser currentUser, String textMessage) {
        SendMessage message = new SendMessage(currentUser.getChatId(), "⬇️⬇️⬇️");
        message.replyMarkup(new ReplyKeyboardRemove(true));
        tgBot.execute(message);
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CHANGE_TOUR_MSG_ADM);
        sendMessage.replyMarkup(keyboardService.addTourAndBackAdminBtns(textMessage));
        currentUser.setState(TelegramState.TOUR);
        currentUser.setChosenCategory(categoryService.getCategoryByName(textMessage).get());
        Message execute = tgBot.execute(sendMessage).message();
        currentUser.getMessageId().add(execute.messageId());
        telegramUserService.updateUser(currentUser);
    }

    @SneakyThrows
    public void sendCategoriesAdmin(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(
                currentUser.getChatId(), BotConstant.CHOOSE_MSG_ADM
        );
        sendMessage.replyMarkup(adminKeyboardService.categoryAdminBtn());
        tgBot.execute(sendMessage);
        currentUser.setState(TelegramState.CATEGORIES);
        telegramUserService.updateUser(currentUser);
    }

}