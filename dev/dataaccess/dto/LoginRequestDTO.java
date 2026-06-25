package dataaccess.dto;

public class LoginRequestDTO {
    private final String userId;
    private final String password;

    public LoginRequestDTO(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}