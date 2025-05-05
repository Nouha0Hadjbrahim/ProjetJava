package service;

import model.Message;
import java.util.ArrayList;
import java.util.List;

public class MessageService {
    private static MessageService instance;
    private List<Message> messages;
    private int messageCount;

    private MessageService() {
        messages = new ArrayList<>();
        messageCount = 0;
    }

    public static MessageService getInstance() {
        if (instance == null) {
            instance = new MessageService();
        }
        return instance;
    }

    public void addMessage(Message message) {
        messages.add(message);
        messageCount++;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public int getUnreadCount() {
        return (int) messages.stream().filter(m -> !m.isLu()).count();
    }

    public void markAsRead(int messageId) {
        messages.stream()
                .filter(m -> m.getId() == messageId)
                .findFirst()
                .ifPresent(m -> m.setLu(true));
    }

    public int getMessageCount() {
        return messageCount;
    }
} 