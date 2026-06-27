package dataaccess.mapper;

import dataaccess.dto.ConstraintDto;
import employee.domain.Constraint;

public class ConstraintMapper {
    public static Constraint toDomain(ConstraintDto constraintDto) {
        if (constraintDto == null) return null;
        return new Constraint(
                constraintDto.getDayOfWeek(),
                constraintDto.getShiftType()
        );
    }

    public static ConstraintDto toDto(Constraint constraint) {
        if (constraint == null) return null;
        return new ConstraintDto(
                constraint.getDay(),
                constraint.getShiftType()
        );
    }
}
