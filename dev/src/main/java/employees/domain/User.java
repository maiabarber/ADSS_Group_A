package employees.domain;

import java.util.Objects;

/**
 * User class represents a user in the system with an ID and password.
 * It provides methods to get and set the ID and password, as well as a method to check if the provided credentials match the user's credentials.
 */
public class User {
    private String id;
    private String password;

    public User(String id, String password) {
        validateId(id);
        this.id = id;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        validateId(id);
        this.id = id;
    }

    private void validateId(String id) {
        if (id == null || !id.matches("\\d{9}")) {
            throw new IllegalArgumentException("ID must be exactly 9 digits");
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean matchesCredentials(String id, String password) {
        return Objects.equals(this.id, id) && Objects.equals(this.password, password);
    }
}
