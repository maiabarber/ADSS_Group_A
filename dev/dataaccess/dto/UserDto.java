package dataaccess.dto;

public final class UserDto {
    private final String userId;
    private final String password;
    private final boolean hrManager;

    public UserDto(String userId, String password, boolean hrManager) {
        this.userId = userId;
        this.password = password;
        this.hrManager = hrManager;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHrManager() {
        return hrManager;
    }


}