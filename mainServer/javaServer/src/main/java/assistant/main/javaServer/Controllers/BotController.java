package assistant.main.javaServer.Controllers;

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

    @GetMapping()
    public ResponseEntity<String> test(){
        System.out.println("test **************** test");
        return ResponseEntity.ok("Hi");
    }

    @PostMapping(value = "/multifile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> handelMultiData(
            @RequestParam("user_id") Long userId,
            @RequestParam("chat_id") Long chatId,
            @RequestParam("message_id") Long messageId,
            @RequestParam(value = "content", required = false) String text,
            @RequestPart(value = "file", required = false)MultipartFile file,
            @RequestParam("msg_type") String msgType){
        System.out.println("give file...");
        if ("PHOTO".equals(msgType) && file != null && !file.isEmpty()){
            try {
                byte[] fileBytes = file.getBytes();
                String fileName = file.getOriginalFilename();
                System.out.println("good photo...");
            }catch (IOException e){
                System.out.println("error in read file: " + e.getMessage());
            }
        }
        String replyText = "پیام دریافت شد";
        Map<String,String> response = new HashMap<>();
        response.put("reply",replyText);
        System.out.println("reply text...");
        return ResponseEntity.ok(response);
    }
}
