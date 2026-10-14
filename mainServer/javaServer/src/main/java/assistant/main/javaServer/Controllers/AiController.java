package assistant.main.javaServer.Controllers;

import assistant.main.javaServer.Services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService messageService;

    @PostMapping("/message")
    public ResponseEntity<String> askAI(@RequestBody String message) {
        return ResponseEntity.ok(messageService.sendAi(message));
    }
}
