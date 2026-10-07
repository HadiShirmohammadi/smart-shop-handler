package assistant.main.javaServer.Dto;

public class AuthResponse {
    private String token;
    private String username;
    private boolean isAdmin;

    public AuthResponse(String token, String username,boolean isAdmin){
        this.token=token;
        this.username=username;
        this.isAdmin=isAdmin;
    }

    public String getToken(){return token;}

    public String getUsername() {
        return username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

