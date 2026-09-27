package assistant.main.javaServer.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class GroqService {

     private final RestClient restClient;
     private final String apiKey;

     public GroqService(
             @Value("${GROQ_API_URL}") String apiUrl,
             @Value("${GROQ_API_KEY}") String apiKey
     ){
          this.restClient = RestClient.builder().baseUrl(apiUrl).build();
          this.apiKey = apiKey;
     }

     public String sendAi(String message){
          Map<String,Object> request = Map.of(
                  "model", "openai/gpt-oss-20b",
                  "messages", List.of(
                          Map.of(
                                  "role","system",
                                  "content"," تو دستیار فروش آنلاین هستی"
                          ),
                          Map.of(

                                  "role", "user",
                                  "content", message
                          )
                  )
          );
          JsonNode response = restClient.post().uri("/chat/completions").header("Authorization", "Bearer " + apiKey)
                  .contentType(MediaType.APPLICATION_JSON)
                  .body(request)
                  .retrieve()
                  .body(JsonNode.class);
          return response.path("choices")
                  .path(0)
                  .path("message")
                  .path("content")
                  .asText();
     }
}
