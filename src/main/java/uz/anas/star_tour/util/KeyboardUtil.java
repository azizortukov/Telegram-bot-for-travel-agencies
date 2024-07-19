package uz.anas.star_tour.util;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyboardUtil {

    public InlineKeyboardButton[][] convertToListOfInlineKeyboardListsToArray(List<List<InlineKeyboardButton>> listOfLists) {
        int rowCount = listOfLists.size();
        int colCount = listOfLists.stream().mapToInt(List::size).max().orElse(0);

        InlineKeyboardButton[][] matrix = new InlineKeyboardButton[rowCount][colCount];

        for (int i = 0; i < rowCount; i++) {
            List<InlineKeyboardButton> row = listOfLists.get(i);
            matrix[i] = row.toArray(new InlineKeyboardButton[0]);
        }

        return matrix;
    }


    public String[][] convertToListOfStringListsToArray(List<List<String>> listOfLists) {
        int rowCount = listOfLists.size();
        int colCount = listOfLists.stream().mapToInt(List::size).max().orElse(0);

        String[][] matrix = new String[rowCount][colCount];

        for (int i = 0; i < rowCount; i++) {
            List<String> row = listOfLists.get(i);
            matrix[i] = row.toArray(new String[0]);
        }

        return matrix;
    }
}
