package uz.anas.star_tour.bot_service;

import com.pengrad.telegrambot.model.request.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.anas.star_tour.bot.BotConstant;
import uz.anas.star_tour.service.CategoryService;
import uz.anas.star_tour.util.KeyboardUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminKeyboardService {


    private final CategoryService categoryService;
    private final KeyboardService keyboardService;
    private final KeyboardUtil keyboardUtil;

    public ReplyKeyboardMarkup categoryAdminBtn() {
        var keyboards = keyboardService.getCollectionRows(categoryService.findAll());

        keyboards.add(List.of(BotConstant.ADD_CAT_BTN_ADM, BotConstant.REM_CAT_BTN_ADN));
        keyboards.add(List.of(BotConstant.CHANGE_CAT_BTN_ADM, BotConstant.ADD_ADM_BTN_ADM));
        keyboards.add(List.of(BotConstant.SHOW_EDT_SDM_BTN, BotConstant.SHOW_APP_SDM_BTN));

        var markup = new ReplyKeyboardMarkup(keyboardUtil.convertToListOfStringListsToArray(keyboards));
        markup.resizeKeyboard(true);
        return markup;
    }

    public Keyboard removeEditor(Long chatId) {
        InlineKeyboardButton remove = new InlineKeyboardButton(BotConstant.REMOVE_SUPP_ADM_BTN);
        InlineKeyboardButton editor = new InlineKeyboardButton(BotConstant.ADD_SUP_ADM_BTN);
        remove.callbackData("remove/" + chatId);
        editor.callbackData("editor/" + chatId);
        return new InlineKeyboardMarkup(keyboardUtil.convertToListOfInlineKeyboardListsToArray(
                List.of(List.of(remove, editor))));
    }

    public ReplyKeyboardMarkup applicationBtn() {
        var buttons = List.of(
                List.of(BotConstant.IN_PROGRESS_SUP_BTN, BotConstant.NO_CONNECTION_SUP_BTN),
                List.of(BotConstant.SUCCESS_SDM_BTN, BotConstant.DENIED_SDM_BTN),
                List.of(BotConstant.REPORT_SDM_BTN),
                List.of(BotConstant.BACK_BTN));

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup( keyboardUtil.convertToListOfStringListsToArray(buttons));
        markup.resizeKeyboard(true);
        return markup;
    }
}
