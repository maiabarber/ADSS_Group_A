package dataaccess.mapper;
import dataaccess.dto.ShiftDto;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.Shift;


public class ShiftMapper {
    public static ShiftDto toDto(Shift shift) {
        return new ShiftDto(
                shift.getDate(),
                shift.getShiftType(),
                shift.getShiftManager().getId(),
                shift.getBranch(),
                new Map<Role, Integer>(),
                shift.getShiftManager() != null ? shift.getShiftManager().getId() : null
        );
    }

    public static Shift toDomain(ShiftDto shiftDto) {
        return new Shift(
                shiftDto.getId(),
                shiftDto.getDate(),
                shiftDto.getShiftType(),
                shiftDto.getRequiredStoreKeeperInput(),
                new Branch(shiftDto.getBranchId()),
                shiftDto.getShiftManagerId() != null ? new Employee(shiftDto.getShiftManagerId()) : null
        );
    }
}
