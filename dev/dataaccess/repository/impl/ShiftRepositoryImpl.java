package dataaccess.repository.impl;

import dataaccess.DatabaseConnection;
import dataaccess.DatabaseInitializer;
import dataaccess.dao.ShiftDaoImpl;
import dataaccess.dto.ShiftDto;
import dataaccess.mapper.ShiftMapper;
import dataaccess.repository.RepositoryException;
import dataaccess.repository.ShiftRepository;
import employee.domain.Shift;
import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShiftRepositoryImpl implements ShiftRepository {
    private final ShiftDaoImpl shiftDao;

    public ShiftRepositoryImpl() {
        try {
            DatabaseInitializer.initializeDatabase();
            Connection connection = DatabaseConnection.getConnection();
            this.shiftDao = new ShiftDaoImpl(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize ShiftRepository", e);
        }
    }

    public ShiftRepositoryImpl(ShiftDaoImpl shiftDao) {
        this.shiftDao = shiftDao;
    }

    @Override
    public Shift save(Shift shift) throws RepositoryException {
        shiftDao.createOrUpdate(ShiftMapper.toDto(shift));
        return shift;
    }

    @Override
    public Optional<Shift> findById(Integer id) throws RepositoryException {
        return Optional.ofNullable(ShiftMapper.toDomain(shiftDao.findbyId(String.valueOf(id))));
    }

    @Override
    public List<Shift> findAll() throws RepositoryException {
        List<Shift> shifts = new ArrayList<>();
        for (ShiftDto dto : shiftDao.findAll()) {
            shifts.add(ShiftMapper.toDomain(dto));
        }
        return shifts;
    }

    @Override
    public void deleteById(Integer id) throws RepositoryException {
        shiftDao.delete(String.valueOf(id));
    }

    public Optional<Shift> findByDateAndType(LocalDate date, ShiftType shiftType) throws RepositoryException {
        for (ShiftDto dto : shiftDao.findAll()) {
            if (date.toString().equals(dto.getShiftDate()) && shiftType.name().equals(dto.getShiftType())) {
                return Optional.of(ShiftMapper.toDomain(dto));
            }
        }
        return Optional.empty();
    }
}
