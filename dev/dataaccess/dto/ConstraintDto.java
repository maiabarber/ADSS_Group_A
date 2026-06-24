package dataaccess.dto;

import employee.domain.ShiftType;

import java.time.DayOfWeek;

public final class ConstraintDto {
    private final DayOfWeek dayOfWeek;
    private final ShiftType shiftType;

    public ConstraintDto(DayOfWeek dayOfWeek, ShiftType shiftType) {
        this.dayOfWeek = dayOfWeek;
        this.shiftType = shiftType;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }
}