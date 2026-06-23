package employee.repository;

import java.time.LocalDate;
import java.util.Optional;

import employee.domain.Shift;
import employee.domain.ShiftType;

public interface ShiftRepository extends Repository<Shift, String> {
        Optional<Shift> findByDateAndType(LocalDate date, ShiftType shiftType);
}
