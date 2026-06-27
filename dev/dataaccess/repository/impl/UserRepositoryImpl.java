package dataaccess.repository.impl;
import dataaccess.dao.UserDAOImpl;
import dataaccess.dto.UserDto;
import dataaccess.repository.UserRepository;

public class UserRepositoryImpl extends AbstractDaoRepository<UserDto, String>
        implements UserRepository {

    public UserRepositoryImpl(UserDAOImpl dao) {
        super(dao);
    }
}