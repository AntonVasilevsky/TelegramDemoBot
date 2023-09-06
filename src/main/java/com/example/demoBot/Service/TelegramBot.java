package com.example.demoBot.Service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private final String name;





    public TelegramBot(String botToken, String name) {
        super(botToken);
        this.name = name;

    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    String name = update.getMessage().getChat().getFirstName();
                    onStartMessageReceived(chatId, name);
                    break;
                case
                default:
                    sendMessage(chatId, "Sorry, this command is not recognized");
            }

        }
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        super.onUpdatesReceived(updates);
    }

    @Override
    public String getBotUsername() {

        return name;
    }


    @Override
    public void onRegister() {
        super.onRegister();
    }
    public void onStartMessageReceived(long chatId, String name) {
        String answer = "Hello " + name + ", you will win a jackpot!";
        sendMessage(chatId, answer);
    }
    public void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage(String.valueOf(chatId), messageToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
