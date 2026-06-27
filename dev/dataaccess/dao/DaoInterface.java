package dataaccess.dao;
import java.util.List;

public interface DaoInterface<T> {
    void createOrUpdate(T e);
    T findbyId(String id);
    void update(T e);
    void delete(String id);
    List<T> findAll();
}