package employees.domain;

public class HR_Manager extends User {
    private boolean isHRManager;

    public HR_Manager() {
    }

    public HR_Manager(String id, String password) {
        super(id, password);
        this.isHRManager = true;
    }

    public boolean isHRManager() {
        return isHRManager;
    }

}
