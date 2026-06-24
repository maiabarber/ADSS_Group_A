package dataaccess.repository;

import dataaccess.dto.UserDto;
import employee.repository.Repository;

public interface UserRepository extends Repository<UserDto, String> {
}