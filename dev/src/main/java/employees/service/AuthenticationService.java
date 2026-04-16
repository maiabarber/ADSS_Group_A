package employees.service;

import employees.domain.User;
import employees.repository.RepositoryException;
import employees.repository.UserRepository;

import java.util.Optional;

public class AuthenticationService {
    private final UserRepository userRepository;
    private User currentUser;

    public AuthenticationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(User user) throws RepositoryException {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("User and user id must not be null");
        }
        userRepository.save(user);
    }

    public Optional<User> login(String id, String password) {
        try {
            Optional<User> matchedUser = userRepository.findById(id)
                .filter(user -> user.matchesCredentials(id, password));

            matchedUser.ifPresent(user -> currentUser = user);
            return matchedUser;
        } catch (RepositoryException e) {
            return Optional.empty();
        }
    }

    public boolean logout() {
        if (currentUser == null) {
            return false;
        }

        currentUser = null;
        return true;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}
