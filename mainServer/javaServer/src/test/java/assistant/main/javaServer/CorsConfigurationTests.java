package assistant.main.javaServer;

import assistant.main.javaServer.Config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.DefaultCorsProcessor;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigurationTests {
    @Test
    void allowsFrontendRegistrationPreflight() throws Exception {
        for (String origin : new String[]{
                "http://127.0.0.1:5173", "http://localhost:5173",
                "http://127.0.0.1:4173", "http://localhost:4173"}) {
            MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/auth/register");
            request.addHeader("Origin", origin);
            request.addHeader("Access-Control-Request-Method", "POST");
            request.addHeader("Access-Control-Request-Headers", "content-type");
            MockHttpServletResponse response = new MockHttpServletResponse();
            var configuration = new SecurityConfig(null).corsConfigurationSource()
                    .getCorsConfiguration(request);
            assertTrue(new DefaultCorsProcessor().processRequest(configuration, request, response));
            assertEquals(origin, response.getHeader("Access-Control-Allow-Origin"));
        }
    }

    @Test
    void rejectsUntrustedOrigins() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/register");
        request.addHeader("Origin", "https://untrusted.example");
        MockHttpServletResponse response = new MockHttpServletResponse();
        var configuration = new SecurityConfig(null).corsConfigurationSource()
                .getCorsConfiguration(request);
        assertFalse(new DefaultCorsProcessor().processRequest(configuration, request, response));
        assertEquals(403, response.getStatus());
    }
}
