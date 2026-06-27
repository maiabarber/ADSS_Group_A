package dataaccess.dao;
import java.util.List;

import dataaccess.repository.RepositoryException;

public interface DaoInterface<T> {
    void createOrUpdate(T e) throws RepositoryException;
    T findbyId(String id) throws RepositoryException;
    void update(T e) throws RepositoryException;
    void delete(String id) throws RepositoryException;
    List<T> findAll() throws RepositoryException;
}