package dataaccess.repository.impl;

import dataaccess.dao.DaoInterface;
import dataaccess.repository.Repository;
import dataaccess.repository.RepositoryException;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDaoRepository<T, ID> implements Repository<T, ID> {

    protected final DaoInterface<T> dao;

    protected AbstractDaoRepository(DaoInterface<T> dao) {
        this.dao = dao;
    }

    @Override
    public T save(T entity) throws RepositoryException {
        dao.createOrUpdate(entity);
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) throws RepositoryException {
        return Optional.ofNullable(dao.findbyId(String.valueOf(id)));
    }

    @Override
    public List<T> findAll() throws RepositoryException {
        return dao.findAll();
    }

    @Override
    public void deleteById(ID id) throws RepositoryException {
        dao.delete(String.valueOf(id));
    }
}