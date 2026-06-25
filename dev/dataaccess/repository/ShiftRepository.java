package dataaccess.repository;

import dataaccess.dto.ShiftDto;
import employee.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface ShiftRepository extends Repository<ShiftDto, String> {
    Optional<ShiftDto> findByDateAndType(LocalDate date, employee.domain.ShiftType shiftType);
}