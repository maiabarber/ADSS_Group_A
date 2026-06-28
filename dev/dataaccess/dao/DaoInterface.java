package dataaccess.dao;

import java.util.List;
import dataaccess.repository.RepositoryException;

public interface DaoInterface<T> {
    void createOrUpdate(T dto) throws RepositoryException;
    T findbyId(String id) throws RepositoryException;
    void update(T dto) throws RepositoryException;
    void delete(String id) throws RepositoryException;
    List<T> findAll() throws RepositoryException;
}
