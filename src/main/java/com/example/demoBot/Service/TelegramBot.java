package com.example.demoBot.Service;



import com.example.demoBot.model.User;
import com.example.demoBot.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${bot.name}")
    private final String name;
    private final UserRepository userRepository;
    private static final String HELP_INFO = """
            This bot was created for demo purposes
            You can choose commands from the menu
            Type /mydata to see your data stored""";


    public TelegramBot(String botToken, String name, UserRepository userRepository) {
        super(botToken);
        this.name = name;
        this.userRepository = userRepository;
        List<BotCommand> botCommandsList = new ArrayList<>();
        botCommandsList.add(new BotCommand("/start", "get a welcome message"));
        botCommandsList.add(new BotCommand("/mydata", "get my stored data"));
        botCommandsList.add(new BotCommand("/deletedata", "delete my stored data"));
        botCommandsList.add(new BotCommand("/help", "bot help info"));
        botCommandsList.add(new BotCommand("/settings", "settings"));
        try {
            execute(new SetMyCommands(botCommandsList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }

    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            switch (messageText){
                case "/start":
                    String name = update.getMessage().getChat().getFirstName();
                    startMessageReceived(chatId, name);
                    registerUser(update.getMessage());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_INFO);
                    break;
                case "/mydata":
                    String response = myDataMessageReceived(chatId);
                    sendMessage(chatId, response);
                    break;
                default:
                    sendMessage(chatId, "Sorry, this command is not recognized");
            }

        }
    }

    private String myDataMessageReceived(Long chatId) {
        return userRepository.findById(chatId).toString();
    }

    private void registerUser(Message message) {
        if(userRepository.findById(message.getChatId()).isEmpty()) {
            Chat chat = message.getChat();

            User user = new User();
            user.setId(message.getChatId());
            user.setName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setNick(chat.getUserName());
            user.setRegistered(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

            log.info(user + " has been registered");
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
    public void startMessageReceived(long chatId, String name) {
        String answer = "Hello " + name + ", sport is good!";
        sendMessage(chatId, answer);
        log.info("replied on /start to user: " + name);

    }
    public void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage(String.valueOf(chatId), messageToSend);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }
}
