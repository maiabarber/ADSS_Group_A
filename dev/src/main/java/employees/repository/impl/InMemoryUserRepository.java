package employees.repository.impl;

import employees.domain.User;
import employees.repository.RepositoryException;
import employees.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryUserRepository class provides an in-memory implementation of the UserRepository interface.
 * It uses a HashMap to store User objects by their ID, allowing for basic CRUD operations.
 */
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> usersById = new HashMap<>();

    @Override
    public User save(User user) throws RepositoryException {
        if (user == null) {
            throw new RepositoryException("User cannot be null");
        }
        if (user.getId() == null || user.getId().isEmpty()) {
            throw new RepositoryException("User id cannot be null or blank");
        }
        usersById.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public List<User> findAll() throws RepositoryException {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return;
        }
        usersById.remove(id);
    }
}