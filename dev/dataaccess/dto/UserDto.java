package dataaccess.dto;

public class UserDto {
    private final String id;
    private final String password;
    private final boolean isHRManager;

    public UserDto(String id, String password, boolean isHRManager) {
        this.id = id;
        this.password = password;
        this.isHRManager = isHRManager;
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHRManager() {
        return isHRManager;
    }
}