package uz.anas.star_tour.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

@Service
@RequiredArgsConstructor
public class LogService {

    static FileHandler dataFileHandler = getDataFileHandler();
    static Path dataFilePath;
    static Logger dataLogger;
    private final TelegramBot telegramBot;

    private static FileHandler getDataFileHandler() {
        if (dataFileHandler == null) {
            try {
                dataFilePath = Path.of("dataLog.txt");
                if (!Files.exists(dataFilePath)) {
                    Files.createFile(dataFilePath);
                }
                dataFileHandler = new FileHandler(String.valueOf(dataFilePath));
                dataFileHandler.setFormatter(new SimpleFormatter());
                dataLogger = Logger.getLogger("DataLog");
                dataLogger.addHandler(dataFileHandler);
                return dataFileHandler;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dataFileHandler;
    }


    public String getStackTraceAsString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    @Async
    public  void dataLog(String strLog) {
        dataLogger.info(strLog);
    }

    @Async
    public  void exceptionLogger(String log) {
        SendMessage sendMessage = new SendMessage(474016858, log);
        telegramBot.execute(sendMessage);
    }
}
