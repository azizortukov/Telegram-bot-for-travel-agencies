package uz.anas.star_tour.bot_service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.bot.BotConstant;
import uz.anas.star_tour.service.ApplicationService;
import uz.anas.star_tour.service.CategoryService;
import uz.anas.star_tour.service.TelegramUserService;
import uz.anas.star_tour.service.TourService;
import uz.anas.star_tour.entity.Application;
import uz.anas.star_tour.entity.Category;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.Tour;
import uz.anas.star_tour.entity.enums.RequestStatus;
import uz.anas.star_tour.entity.enums.Role;
import uz.anas.star_tour.entity.enums.TelegramState;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;


@Service
@RequiredArgsConstructor
public class AdminBotService {

    private final BotService botService;
    private final TourService tourService;
    private final TelegramUserService telegramUserService;
    private final TelegramBot tgBot;
    private final LogService logService;
    private final KeyboardService keyboardService;
    private final AdminKeyboardService adminKeyboardService;
    private final ApplicationService applicationService;
    private final ScheduledServices scheduledServices;
    private final CategoryService categoryService;

    @SneakyThrows
    public void checkInsideCategoriesAndTakeAction(TelegramUser currentUser, String data) {
        if (data.equals(BotConstant.BACK_BTN)) {
            botService.clearMessage(currentUser);
            sendCategoriesAdmin(currentUser);
            return;
        }
        if (currentUser.getState().equals(TelegramState.TOUR)) {
            if (tourService.getTourById(data).isPresent()) {
                Optional<Tour> tour = tourService.getTourById(data);
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "♻️Tur muvvafaqiyatli o'chirildi");
                tourService.delete(tour.get());
                currentUser.setState(TelegramState.CATEGORIES);
                telegramUserService.updateUser(currentUser);
                tgBot.execute(sendMessage);
            } else if (categoryService.getCategoryById(data).isPresent()) {
                Optional<Category> category = categoryService.getCategoryById(data);
                currentUser.setChosenCategory(category.get());
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.PAY_ATTENTION_ADM_MSG);
                currentUser.setState(TelegramState.ADD_TOUR);
                telegramUserService.updateUser(currentUser);
                tgBot.execute(sendMessage);
            }
        } else if (currentUser.getState().equals(TelegramState.OPERATOR)) {
            String option = data.split("/")[0];
            Long chatId = Long.valueOf(data.split("/")[1]);
            TelegramUser user = telegramUserService.checkUser(chatId);
            if (user != null) {
                if (option.equals("remove")) {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), user.getFiringDetails());
                    logService.dataLog("Operator was removed: " + user.getPhoneNumber());
                    tgBot.execute(sendMessage);
                    telegramUserService.deleteUser(chatId);
                } else if (option.equals("editor")) {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), user.getAcceptEditorDetails());
                    logService.dataLog("Operator exceeded to editor: " + user.getPhoneNumber());
                    tgBot.execute(sendMessage);
                    user.setRole(Role.EDITOR);
                    user.setState(TelegramState.CATEGORIES);
                    telegramUserService.updateUser(user);
                }
            }
            sendCategoriesAdmin(currentUser);
        } else if (currentUser.getState().equals(TelegramState.ADD_OPERATOR)) {
            TelegramUser user = telegramUserService.checkUser(Long.valueOf(data));
            user.setRole(Role.SUPPORT);
            user.setState(TelegramState.START);
            telegramUserService.updateUser(user);
            logService.dataLog("Operator was added: " + user.getPhoneNumber());
            botService.clearMessage(currentUser);
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_ADM_ADD_MSG);
            tgBot.execute(sendMessage);
            sendCategoriesAdmin(currentUser);
        } else if (currentUser.getState().equals(TelegramState.EDITORS)) {
            String option = data.split("/")[0];
            Long chatId = Long.valueOf(data.split("/")[1]);
            TelegramUser user = telegramUserService.checkUser(chatId);
            if (user != null) {
                if (option.equals("remove")) {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), user.getFiringDetails());
                    logService.dataLog("Editor was removed: " + user.getPhoneNumber());
                    tgBot.execute(sendMessage);
                    telegramUserService.deleteUser(chatId);
                } else if (option.equals("editor")) {
                    SendMessage sendMessage = new SendMessage(currentUser.getChatId(), user.getAcceptOperatorDetails());
                    logService.dataLog("Editor decreased to operator: " + user.getPhoneNumber());
                    tgBot.execute(sendMessage);
                    user.setRole(Role.SUPPORT);
                    user.setState(TelegramState.CATEGORIES);
                    telegramUserService.updateUser(user);
                }
            }
            sendCategoriesAdmin(currentUser);
        } else if (currentUser.getState().equals(TelegramState.ADD_EDITOR)) {
            TelegramUser user = telegramUserService.checkUser(Long.valueOf(data));
            user.setRole(Role.EDITOR);
            user.setState(TelegramState.CATEGORIES);
            telegramUserService.updateUser(user);
            logService.dataLog("Editor was added: " + user.getPhoneNumber());
            botService.clearMessage(currentUser);
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CNF_EDT_ADD_MSG);
            tgBot.execute(sendMessage);
            sendCategoriesAdmin(currentUser);
        } else if (currentUser.getState().equals(TelegramState.ADD_TOUR)) {
            Optional<Tour> tour = tourService.getTourById(data);
            if (tour.isPresent()) {
                SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "✅ Mijozlarga bu tur haqida" +
                        "ma'lumot junatildi");
                tgBot.execute(sendMessage);
                scheduledServices.sendTourToUsers(tour.get());
            }
            sendCategoriesAdmin(currentUser);
        }
    }

    public boolean checkForBack(TelegramUser currentUser, String textMessage) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            sendCategoriesAdmin(currentUser);
            return true;
        } else if (textMessage.equals("/start")) {
            sendApplicationCategories(currentUser);
        }

        return false;
    }

    @SneakyThrows
    public void checkCategoriesAndTakeAction(TelegramUser currentUser, String textMessage) {
        List<Tour> tours = tourService.getTourByCategoryNameNull(textMessage);
        if (tours != null) {
            botService.showToursToChange(currentUser, tours);
        } else if (categoryService.getCategoryByName(textMessage).isPresent()) {
            botService.askTourForChosenCategory(currentUser, textMessage);
        } else if (textMessage.equals(BotConstant.ADD_CAT_BTN_ADM)) {
            botService.addCategory(currentUser);
        } else if (textMessage.equals(BotConstant.REM_CAT_BTN_ADN)) {
            botService.chooseCategoryToRemove(currentUser);
        } else if (textMessage.equals(BotConstant.ADD_ADM_BTN_ADM)) {
            botService.showOperators(currentUser, true);
        } else if (textMessage.equals(BotConstant.CHANGE_CAT_BTN_ADM)) {
            botService.chooseCategoryToChange(currentUser);
        } else if (textMessage.equals(BotConstant.SHOW_EDT_SDM_BTN)) {
            showEditors(currentUser);
        } else if (textMessage.equals(BotConstant.SHOW_APP_SDM_BTN)) {
            sendApplicationCategories(currentUser);
        } else {
            sendCategoriesAdmin(currentUser);
        }
    }

    private void showEditors(TelegramUser currentUser) {
        if (telegramUserService.getEditors().isEmpty()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.NO_EDITOR_ADM_MSG);
            sendMessage.replyMarkup(keyboardService.addAdminAndBackBtns());
            currentUser.setState(TelegramState.EDITORS);
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
            return;
        }
        for (TelegramUser editor : telegramUserService.getEditors()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(),
                    "Editor nomer telefon: " + editor.getPhoneNumber());
            sendMessage.replyMarkup(adminKeyboardService.removeEditor(editor.getChatId()));
            tgBot.execute(sendMessage);
        }
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.ADD_EDITOR_ADM_MSG);
        sendMessage.replyMarkup(keyboardService.addAdminAndBackBtns());
        currentUser.setState(TelegramState.EDITORS);
        Message execute = tgBot.execute(sendMessage).message();
        currentUser.getMessageId().add(execute.messageId());
        telegramUserService.updateUser(currentUser);
    }

    @SneakyThrows
    private void sendApplicationCategories(TelegramUser currentUser) {
        SendMessage sendMessage = new SendMessage(currentUser.getChatId(), BotConstant.CHOOSE_SDM_BTN);
        sendMessage.replyMarkup(adminKeyboardService.applicationBtn());
        currentUser.setState(TelegramState.SHOW_APPLICATION);
        telegramUserService.updateUser(currentUser);
        tgBot.execute(sendMessage);
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

    @SneakyThrows
    public void addEditorOrBack(TelegramUser currentUser, String textMessage) {
        if (textMessage.equals(BotConstant.BACK_BTN)) {
            sendCategoriesAdmin(currentUser);
        } else if (textMessage.equals(BotConstant.ADD_TOUR_ADM_BTN)) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "Editorga qushmoqchi bugan " +
                    "odamingiz start tugmasini bosib, kontakt junatish tugmasini bosgan bo'lishi kerak");
            sendMessage.replyMarkup(keyboardService.showUsers());
            currentUser.setState(TelegramState.ADD_EDITOR);
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
        }
    }

    public void checkApplication(TelegramUser currentUser, String textMessage, Message message) {
        currentUser.getMessageId().add(message.messageId());
        botService.clearMessage(currentUser);
        if (checkForBack(currentUser, textMessage)) {
            return;
        }
        switch (textMessage) {
            case BotConstant.DENIED_SDM_BTN -> sendApplicationDetails(currentUser, RequestStatus.DONE_REJECT);
            case BotConstant.SUCCESS_SDM_BTN -> sendApplicationDetails(currentUser, RequestStatus.DONE_SUCCESS);
            case BotConstant.IN_PROGRESS_SUP_BTN -> sendApplicationDetails(currentUser, RequestStatus.IN_PROGRESS);
            case BotConstant.NO_CONNECTION_SUP_BTN -> sendApplicationDetails(currentUser, RequestStatus.NO_ANSWER_CONNECTION);
            case BotConstant.REPORT_SDM_BTN -> createReport(currentUser);
        }
    }

    @SneakyThrows
    private void sendApplicationDetails(TelegramUser currentUser, RequestStatus requestStatus) {
        if (applicationService.getApplicationByStatus(requestStatus).isEmpty()) {
            SendMessage sendMessage = new SendMessage(currentUser.getChatId(), "Bu statusda so'rovnoma yuq");
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
            telegramUserService.updateUser(currentUser);
            return;
        }
        for (Application application : applicationService.getApplicationByStatus(requestStatus)) {
            SendMessage sendMessage = new SendMessage(
                    currentUser.getChatId(), application.infoForSuperAdmin()
            );
            sendMessage.parseMode(ParseMode.Markdown);
            Message execute = tgBot.execute(sendMessage).message();
            currentUser.getMessageId().add(execute.messageId());
        }
        telegramUserService.updateUser(currentUser);
    }

    public void createReport(TelegramUser currentUser) {

        Map<Month, List<Application>> applicationMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Application application : applicationService.getAllApplication()) {
            Month createdMonth = LocalDateTime.parse(application.getDoneTime(), formatter).getMonth();
            applicationMap.computeIfAbsent(createdMonth, k -> new ArrayList<>()).add(application);
        }
        List<String> filePaths = new ArrayList<>();

        for (Map.Entry<Month, List<Application>> applications : applicationMap.entrySet()) {
            int rowNum = 1;
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Turlar");
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Tur paket",
                        "Status",
                        "Mijoz telefon raqami",
                        "Operator telefon raqami",
                        "Mijoz surov qilgan vaqt",
                        "Operator javob bergan vaqt",
                        "Operator zametkasi"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
                for (Application archive : applications.getValue()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(archive.getTitle());
                    row.createCell(1).setCellValue(archive.statusText());
                    row.createCell(2).setCellValue(archive.getPhoneNumber());
                    row.createCell(3).setCellValue(archive.getOperatorNumber());
                    row.createCell(4).setCellValue(archive.getCreatedTime());
                    row.createCell(5).setCellValue(archive.getDoneTime());
                    row.createCell(6).setCellValue(archive.getDescription());
                }
                String filePath = applications.getKey().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("uz")) + "_oyi.xlsx";
                try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                    workbook.write(outputStream);
                    sendFile(filePath, currentUser);
                    filePaths.add(filePath);
                }
            } catch (IOException e) {
                logService.exceptionLogger(logService.getStackTraceAsString(e));
            }
        }

        deleteFiles(filePaths);
        sendApplicationCategories(currentUser);
    }

    @SneakyThrows
    private void deleteFiles(List<String> filePaths) {
        for (String filePath : filePaths) {
            Files.delete(Paths.get(filePath));
        }
    }

    @SneakyThrows
    private void sendFile(String filePath, TelegramUser currentUser) {
        SendDocument sendDocument = new SendDocument(currentUser.getChatId(), new File(filePath));
        tgBot.execute(sendDocument);
    }
}
