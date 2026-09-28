package assistant.main.javaServer.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long chatId;
    @Column(nullable = false)
    private Long messageId;
    @Column
    private String username;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(columnDefinition = "TEXT",nullable = false)
    private String aiContent;
    @Column(nullable = false)
    private String msgType;
    @Column
    private String filePath;


    public Message(Long userId, Long chatId, Long messageId, String username, String content, String aiContent, String msgType, String filePath) {
        this.userId = userId;
        this.chatId = chatId;
        this.messageId = messageId;
        this.username = username;
        this.content = content;
        this.aiContent = aiContent;
        this.msgType = msgType;
        this.filePath = filePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAiContent() {
        return aiContent;
    }

    public void setAiContent(String aiContent) {
        this.aiContent = aiContent;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
