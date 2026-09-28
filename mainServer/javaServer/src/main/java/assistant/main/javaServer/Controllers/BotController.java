package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Entities.Message;
import assistant.main.javaServer.Repository.MessageRepository;
import assistant.main.javaServer.Services.AiService;
import assistant.main.javaServer.Services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bot")
public class BotController {
    @Autowired
    private final AiService aiService;
    @Autowired
    private MessageService messageService;

    public BotController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/multifile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> handelMultiData(
            @RequestParam("user_id") Long userId,
            @RequestParam("chat_id") Long chatId,
            @RequestParam("message_id") Long messageId,
            @RequestParam(value = "username",required = false) String username,
            @RequestParam(value = "content", required = false) String text,
            @RequestPart(value = "file", required = false)MultipartFile file,
            @RequestParam("msg_type") String msgType){
        System.out.println("give file...");
        System.out.println(username);
        String fileName="";
        if ("PHOTO".equals(msgType) && file != null && !file.isEmpty()){
            try {
                byte[] fileBytes = file.getBytes();
                fileName = file.getOriginalFilename();
                System.out.println("good photo...");
            }catch (IOException e){
                System.out.println("error in read file: " + e.getMessage());
            }
        }
        String replyText = aiService.sendAi(text);
        System.out.println(replyText);
        Map<String,String> response = new HashMap<>();
        response.put("reply",replyText);
        System.out.println("reply text...");
        System.out.println(fileName);
        Message message = new Message(userId,chatId,messageId,username,text, replyText,msgType, fileName);
        Message saveMessage = messageService.saveMessage(message);
        return ResponseEntity.ok(response);
    }
}
