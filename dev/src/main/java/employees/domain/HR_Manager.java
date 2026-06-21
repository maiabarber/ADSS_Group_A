package employees.domain;

/**
 * HR_Manager class represents a Human Resources Manager in the system.
 * It extends the User class and has additional properties specific to HR management.
 */
public class HR_Manager extends User {
    private boolean isHRManager;

    public HR_Manager(String id, String password) {
        super(id, password);
        this.isHRManager = true;
    }

    public boolean isHRManager() {
        return isHRManager;
    }

}
