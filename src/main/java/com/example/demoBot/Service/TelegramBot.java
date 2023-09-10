package com.example.demoBot.Service;



import com.example.demoBot.model.User;
import com.example.demoBot.repositories.UserRepository;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {


    private String name;

    private final Long botOwner;

    private final UserRepository userRepository;

    private static final String HELP_INFO = """
            This bot was created for demo purposes
            You can choose commands from the menu
            For example:
            Type /mydata to see your data stored""";
    private static final String CONFIRM_BUTTON = "CONFIRM_BUTTON";
    private static final String DENY_BUTTON = "DENY_BUTTON";
    private static final String ERROR_OCCURRED = "Error occurred: ";


    @Autowired
    public TelegramBot(String botToken, String name, Long botOwner, UserRepository userRepository) {
        super(botToken);
        this.name = name;
        this.botOwner = botOwner;
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

            if(messageText.contains("/send") && update.getMessage().getChat().getId().equals(botOwner)) {
                String messageToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));

                userRepository.findAll().forEach(user -> configureAndSendMessage(user.getId(), messageToSend));
            }else {

                switch (messageText) {
                    case "/start":
                        String name = update.getMessage().getChat().getFirstName();
                        startMessageReceived(chatId, name);
                        registerUser(update.getMessage());
                        System.out.println("Owner " + botOwner + "bot name " + this.name);
                        break;
                    case "/help":
                        configureAndSendMessage(chatId, HELP_INFO);
                        break;
                    case "/mydata":
                        String response = myDataMessageReceived(chatId);
                        configureAndSendMessage(chatId, response);
                        break;
                    case "/register":
                        register(chatId);
                        break;


                    default:
                        configureAndSendMessage(chatId, "Sorry, this command is not recognized");
                }
            }

        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(data.equals("CONFIRM_BUTTON")) {
                String text = "You have pressed confirm button";
                executeEditMessageText(chatId, text, messageId);

            } else if (data.equals("DENY_BUTTON")) {
                String text = "You have pressed deny button";
                executeEditMessageText(chatId, text, messageId);

            }
        }
    }




    private void register(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Confirm registration action please");

        InlineKeyboardMarkup inlineMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton confirmButton = new InlineKeyboardButton();
        confirmButton.setText("Confirm");
        confirmButton.setCallbackData(CONFIRM_BUTTON);

        InlineKeyboardButton denyButton = new InlineKeyboardButton();
        denyButton.setText("Deny");
        denyButton.setCallbackData(DENY_BUTTON);

        buttons.add(confirmButton);
        buttons.add(denyButton);

        keyboard.add(buttons);

        inlineMarkup.setKeyboard(keyboard);

        message.setReplyMarkup(inlineMarkup);
        executeMessage(message);



    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_OCCURRED + e.getMessage());
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
    private void startMessageReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Hello " + name + ", sport is good!" + ":muscle:");
        sendMessage(chatId, answer);
        log.info("replied on /start to user: " + name);

    }
    private void configureAndSendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(messageToSend);
        executeMessage(message);

    }
    private void sendMessage(long chatId, String messageToSend) {
        SendMessage message = new SendMessage(String.valueOf(chatId), messageToSend);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rowList = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(new KeyboardButton("/check weather"));
        row.add(new KeyboardButton("/sport news"));
        row.add(new KeyboardButton("/register"));
        rowList.add(row);

        row = new KeyboardRow();

        row.add(new KeyboardButton("/exchange rates"));
        row.add(new KeyboardButton("/top movies"));
        row.add(new KeyboardButton("/evening show"));
        rowList.add(row);

        markup.setKeyboard(rowList);

        message.setReplyMarkup(markup);


        executeMessage(message);

    }

    private void executeEditMessageText(long chatId, String text, int messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId(messageId);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(ERROR_OCCURRED + e.getMessage());
        }
    }
}
