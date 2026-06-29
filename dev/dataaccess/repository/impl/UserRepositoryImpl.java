package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.UserDAOImpl;
import dataaccess.dto.UserDto;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.UserRepository;
import employee.domain.Employee;
import employee.domain.HR_Manager;
import employee.domain.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final UserDAOImpl userDao;
    private final Map<String, User> savedUsers = new LinkedHashMap<>();

    public UserRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize UserRepository", e);
        }
        this.userDao = null;
    }

    public UserRepositoryImpl(UserDAOImpl userDao) {
        this.userDao = userDao;
    }

    public boolean isHrManager(User user) {
            if (user == null) {
                return false;
            }

            try {
                 return findById(user.getId()).map(User::isHRManager).orElse(false);
            } catch (RepositoryException e) {
                return false;
            }
        }

    @Override
    public User save(User user) throws RepositoryException {
        if (user == null) {
            return null;
        }

        savedUsers.put(user.getId(), user);

        if (user instanceof Employee) {
            new EmployeeRepositoryImpl().save((Employee) user);
            return user;
        }

        if (userDao != null) {
            userDao.createOrUpdate(toDto(user));
            return user;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            new UserDAOImpl(connection).createOrUpdate(toDto(user));
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save user", e);
        }
        return user;
    }

    @Override
    public Optional<User> findById(String id) throws RepositoryException {
        UserDto dto = findUserDto(id);
        if (dto != null && dto.isHrManager()) {
            return Optional.of(new HR_Manager(dto.getUserId(), dto.getPassword()));
        }

        User savedUser = savedUsers.get(id);
        if (savedUser != null) {
            return Optional.of(savedUser);
        }

        Optional<Employee> employee = new EmployeeRepositoryImpl().findById(id);
        if (employee.isPresent()) {
            return Optional.of(employee.get());
        }

        return Optional.ofNullable(toDomain(dto));
    }

    @Override
    public List<User> findAll() throws RepositoryException {
        Map<String, User> users = new LinkedHashMap<>();
        users.putAll(savedUsers);
        if (userDao != null) {
            for (UserDto dto : userDao.findAll()) {
                users.putIfAbsent(dto.getUserId(), toDomain(dto));
            }
            return new ArrayList<>(users.values());
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            for (UserDto dto : new UserDAOImpl(connection).findAll()) {
                users.putIfAbsent(dto.getUserId(), toDomain(dto));
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to load users", e);
        }
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        savedUsers.remove(id);
        new EmployeeRepositoryImpl().deleteById(id);

        if (userDao != null) {
            userDao.delete(id);
            return;
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            new UserDAOImpl(connection).delete(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete user", e);
        }
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getPassword(),
                user instanceof HR_Manager && ((HR_Manager) user).isHRManager()
        );
    }

    private User toDomain(UserDto dto) {
        if (dto == null) {
            return null;
        }
        return dto.isHrManager()
                ? new HR_Manager(dto.getUserId(), dto.getPassword())
                : new User(dto.getUserId(), dto.getPassword());
    }

    private UserDto findUserDto(String id) throws RepositoryException {
        if (userDao != null) {
            return userDao.findbyId(id);
        }

        try (Connection connection = DatabaseConnection.getConnection()) {
            return new UserDAOImpl(connection).findbyId(id);
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find user", e);
        }
    }
}
