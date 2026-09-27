package assistant.main.javaServer.Entities;

import jakarta.persistence.*;

@Entity
@Table(name = "BotMessage")
public class BotMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private Long chatId;
    @Column(nullable = false)
    private Long messageId;
    private Long username;
    private Long content;
    

}
