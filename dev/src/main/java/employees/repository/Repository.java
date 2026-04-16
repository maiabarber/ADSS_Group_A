package employees.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    T save(T entity) throws RepositoryException;
    Optional<T> findById(ID id) throws RepositoryException;
    List<T> findAll() throws RepositoryException;
    void deleteById(ID id) throws RepositoryException;
}
