package dataaccess.mapper;

import dataaccess.dto.ShiftDto;
import employee.domain.Branch;
import employee.domain.Shift;
import employee.domain.ShiftType;

import java.time.LocalDate;

public final class ShiftMapper {
    private ShiftMapper() {}

    public static ShiftDto toDto(Shift shift) {
        if (shift == null) {
            return null;
        }
        Integer branchId = null;
        if (shift.getBranch() != null) {
            try {
                branchId = Integer.parseInt(shift.getBranch().getBranchId());
            } catch (NumberFormatException ignored) {
                branchId = null;
            }
        }
        return new ShiftDto(
                0,
                shift.getDate() == null ? null : shift.getDate().toString(),
                shift.getShiftType() == null ? null : shift.getShiftType().name(),
                branchId
        );
    }

    public static Shift toDomain(ShiftDto dto) {
        if (dto == null) {
            return null;
        }
        Branch branch = dto.getBranchId() == null
                ? null
                : new Branch(String.valueOf(dto.getBranchId()), "Branch " + dto.getBranchId(), "Unknown");
        return new Shift(
                LocalDate.parse(dto.getShiftDate()),
                ShiftType.valueOf(dto.getShiftType()),
                null,
                0,
                0,
                branch
        );
    }
}
