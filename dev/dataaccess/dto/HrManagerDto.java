package dataaccess.dto;

public final class HrManagerDto extends UserDto {
    private final boolean hrManager;

    public HrManagerDto(String id, String password, boolean hrManager) {
        super(id, password);
        this.hrManager = hrManager;
    }

    public boolean isHrManager() {
        return hrManager;
    }
}