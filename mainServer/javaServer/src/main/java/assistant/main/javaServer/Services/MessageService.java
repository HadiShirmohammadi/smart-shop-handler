package assistant.main.javaServer.Services;

import assistant.main.javaServer.Entities.Message;
import assistant.main.javaServer.Repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message){return messageRepository.save(message);}
}
