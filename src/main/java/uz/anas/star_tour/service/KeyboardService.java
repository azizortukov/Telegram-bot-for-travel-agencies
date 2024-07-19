package uz.anas.star_tour.service;

import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.bot.BotConstant;
import uz.anas.star_tour.db.CategoryService;
import uz.anas.star_tour.db.TelegramUserService;
import uz.anas.star_tour.entity.Application;
import uz.anas.star_tour.entity.Category;
import uz.anas.star_tour.entity.TelegramUser;
import uz.anas.star_tour.entity.Tour;
import uz.anas.star_tour.util.KeyboardUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeyboardService {

    private final CategoryService categoryService;
    private final TelegramUserService telegramUserService;
    private final KeyboardUtil keyboardUtil;

    public static ReplyKeyboardMarkup contactBtn() {
        KeyboardButton keyboardButton = new KeyboardButton(BotConstant.SHARE_CONTACT_BTN);
        keyboardButton.requestContact(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardButton);
        replyKeyboardMarkup.resizeKeyboard(true);
        replyKeyboardMarkup.oneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup categoryBtns() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(
                keyboardUtil.convertToListOfStringListsToArray(getCollectionRows(categoryService.findAll())));
        markup.resizeKeyboard(true);
        return markup;
    }

    public List<List<String>> getCollectionRows(List<Category> categories) {
        List<List<String>> rows = new ArrayList<>();
        List<String> buttons = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            buttons.add(categories.get(i).getName());
            if (i % 2 != 0) {
                rows.add(buttons);
                buttons = new ArrayList<>();
            }
        }
        if (!buttons.isEmpty()) {
            rows.add(buttons);
            buttons = new ArrayList<>();
        }
        rows.add(buttons);
        return rows;
    }

    public ReplyKeyboardMarkup backBtn() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(new KeyboardButton(BotConstant.BACK_BTN));
        replyKeyboardMarkup.resizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public Keyboard tourBtns(Tour tour) {
        InlineKeyboardButton button = new InlineKeyboardButton(BotConstant.MORE_INFO_INLINE);
        button.callbackData(tour.getId());
        return new InlineKeyboardMarkup(button);
    }

    public ReplyKeyboardMarkup categoryAdminBtns() {
        List<List<String>> collectionRows = getCollectionRows( categoryService.findAll() );
        collectionRows.add(List.of( BotConstant.ADD_CAT_BTN_ADM, BotConstant.REM_CAT_BTN_ADN ));
        collectionRows.add( List.of( BotConstant.CHANGE_CAT_BTN_ADM ));
        collectionRows.add( List.of( BotConstant.ADD_ADM_BTN_ADM) );
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup( keyboardUtil.convertToListOfStringListsToArray(collectionRows));
        markup.resizeKeyboard(true);
        return markup;
    }

    public Keyboard tourAdminBtn(Tour tour) {
        var changeButton = new InlineKeyboardButton(BotConstant.CHANGE_TOUR_BTN_ADM);
        var addButton = new InlineKeyboardButton(BotConstant.ADD_TOUR_ADM_BTN);

        changeButton.callbackData(tour.getId());
        addButton.callbackData(tour.getCategoryId());

        var buttons = List.of(List.of(changeButton, addButton));
        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }

    public Keyboard addTourAndBackAdminBtns(String textMessage) {
        var add = new InlineKeyboardButton(BotConstant.ADD_TOUR_ADM_BTN);
        Optional<Category> categoryByName = categoryService.getCategoryByName(textMessage);
        categoryByName.ifPresent(category -> add.callbackData(category.getId()));

        var back = new InlineKeyboardButton(BotConstant.BACK_BTN);
        back.callbackData(BotConstant.BACK_BTN);

        var buttons = List.of( List.of(add), List.of(back));
        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }

    public ReplyKeyboardMarkup addAdminAndBackBtns() {
        var replyKeyboardMarkup = new ReplyKeyboardMarkup(BotConstant.ADD_TOUR_ADM_BTN, BotConstant.BACK_BTN);
        replyKeyboardMarkup.resizeKeyboard(true);
        replyKeyboardMarkup.oneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public Keyboard removeSupport(Long chatId, boolean isAdmin) {
        var remove = new InlineKeyboardButton(BotConstant.REMOVE_SUPP_ADM_BTN);
        if (isAdmin) {
            var editor = new InlineKeyboardButton(BotConstant.ADD_EDT_ADM_BTN);
            remove.callbackData("remove/" + chatId);
            editor.callbackData("editor/" + chatId);
            return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(List.of(List.of(remove, editor))));
        }else {
            remove.callbackData(chatId.toString());
            return new InlineKeyboardMarkup(remove);
        }
    }

    public Keyboard showUsers() {
        List<TelegramUser> clients = telegramUserService.getClients();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> rows = new ArrayList<>();
        int i = 0;
        for (TelegramUser user : clients) {
            if (user.getPhoneNumber() != null) {
                ++i;
                var button = new InlineKeyboardButton(user.getPhoneNumber());
                button.callbackData(user.getChatId().toString());
                rows.add(button);
            }
            if (i % 2 == 0) {
                buttons.add(rows);
                rows = new ArrayList<>();
            }
        }
        buttons.add(rows);
        rows = new ArrayList<>();
        var back = new InlineKeyboardButton(BotConstant.BACK_BTN);
        back.callbackData(BotConstant.BACK_BTN);
        rows.add(back);
        buttons.add(rows);

        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }

    public ReplyKeyboardMarkup supportBtns() {
        var markup = new ReplyKeyboardMarkup(BotConstant.IN_PROGRESS_SUP_BTN, BotConstant.NO_CONNECTION_SUP_BTN);
        markup.resizeKeyboard(true);
        return markup;
    }

    public Keyboard changeProgressAppsStatus(Application application) {
        var noConnection = new InlineKeyboardButton("❌ Kutarmadi");
        noConnection.callbackData(BotConstant.NO_RES_DAT_SUP + application.getId());
        var interested = new InlineKeyboardButton("\uD83C\uDFAF Qiziqdi");
        interested.callbackData(BotConstant.INT_DAT_SIP + application.getId());
        var notInterested = new InlineKeyboardButton("\uD83D\uDE15 Tug’ri kelmadi");
        notInterested.callbackData(BotConstant.NOT_INT_DAT_SUP + application.getId());

        var buttons = List.of(List.of(notInterested, noConnection), List.of(interested));
        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }

    public Keyboard changeNoConnectionAppsStatus(Application application) {
        var interested = new InlineKeyboardButton("\uD83C\uDFAF Qiziqdi");
        interested.callbackData(BotConstant.INT_DAT_SIP + application.getId());
        var notInterested = new InlineKeyboardButton("\uD83D\uDE15 Tug’ri kelmadi");
        notInterested.callbackData(BotConstant.NOT_INT_DAT_SUP + application.getId());

        var buttons = List.of( List.of(notInterested), List.of(interested) );
        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }

    public Keyboard sendNewTourToClients(Tour tour) {
        var interested = new InlineKeyboardButton(BotConstant.SEND_MSG_CUS_BTN);
        interested.callbackData(tour.getId());
        var notInterested = new InlineKeyboardButton(BotConstant.BACK_BTN);
        notInterested.callbackData(BotConstant.BACK_BTN);

        var buttons = List.of( List.of(notInterested), List.of(interested) );

        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(buttons));
    }
}
