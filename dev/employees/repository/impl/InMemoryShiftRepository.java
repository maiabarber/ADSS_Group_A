package employees.repository.impl;

import employees.domain.Shift;
import employees.repository.RepositoryException;
import employees.repository.ShiftRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryShiftRepository class provides an in-memory implementation of the ShiftRepository interface.
 * It uses a HashMap to store Shift objects by a unique ID, allowing for basic CRUD operations.
 */
public class InMemoryShiftRepository implements ShiftRepository {
    private final Map<String, Shift> shiftsById = new HashMap<>();

    @Override
    public Shift save(Shift shift) throws RepositoryException {
        if (shift == null) {
            throw new RepositoryException("Shift cannot be null");
        }
        if (shift.getDate() == null || shift.getShiftType() == null) {
            throw new RepositoryException("Shift date and shift type cannot be null");
        }

        shiftsById.put(buildShiftId(shift), shift);
        return shift;
    }

    @Override
    public Optional<Shift> findById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(shiftsById.get(id));
    }

    @Override
    public List<Shift> findAll() throws RepositoryException {
        return new ArrayList<>(shiftsById.values());
    }

    @Override
    public void deleteById(String id) throws RepositoryException {
        if (id == null || id.isEmpty()) {
            return;
        }
        shiftsById.remove(id);
    }

    private static String buildShiftId(Shift shift) {
        return shift.getDate() + ":" + shift.getShiftType();
    }
}