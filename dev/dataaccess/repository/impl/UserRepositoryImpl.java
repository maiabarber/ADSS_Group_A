package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.UserDAOImpl;
import dataaccess.dto.UserDto;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.UserRepository;
import employee.domain.HR_Manager;
import employee.domain.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final UserDAOImpl userDao;

    public UserRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
            Connection connection = DatabaseConnection.getConnection();
            this.userDao = new UserDAOImpl(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize UserRepository", e);
        }
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
        userDao.createOrUpdate(toDto(user));
        return user;
    }

    @Override
    public Optional<User> findById(String id) throws RepositoryException {
        return Optional.ofNullable(toDomain(userDao.findbyId(id)));
    }

    @Override
    public List<User> findAll() throws RepositoryException {
        List<User> users = new ArrayList<>();
        for (UserDto dto : userDao.findAll()) {
            users.add(toDomain(dto));
        }
        return users;
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        userDao.delete(id);
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
}
