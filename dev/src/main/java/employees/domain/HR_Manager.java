package employees.domain;

public class HR_Manager extends User {
    private boolean isManager;

    public HR_Manager() {
    }

    public HR_Manager(String id, String password) {
        super(id, password);
        this.isManager = true;
    }

    public boolean isManager() {
        return isManager;
    }

}
