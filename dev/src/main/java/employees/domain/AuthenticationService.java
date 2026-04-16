package employees.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthenticationService {
    private final Map<String, User> usersById = new HashMap<>();

    public void registerUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user id must not be null");
        }
        usersById.put(user.getId(), user);
    }

    public Optional<User> login(String id, String password) {
        User user = usersById.get(id);
        if (user == null || !user.matchesCredentials(id, password)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
