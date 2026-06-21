package employee.domain;

import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * DriverAvailability is the employee-module system record that tracks which days of the week
 * and which shift types a driver is available to work for shift scheduling purposes.
 *
 * <p>This record is maintained by HR managers within the employee/shift-assignment system.
 * It is linked to the transportation module's {@code Driver} record through the shared
 * {@code employeeId} — both records refer to the same physical employee.
 *
 * <ul>
 *   <li><b>Transportation Driver record</b> — holds license types and truck eligibility.</li>
 *   <li><b>DriverAvailability (this class)</b> — holds the days/shifts the driver is permitted
 *       to work, used by the shift assignment system.</li>
 * </ul>
 */
public class DriverAvailability {

    private final String employeeId;

    /** Maps each day of the week to the set of shift types the driver can work on that day. */
    private final Map<DayOfWeek, Set<ShiftType>> availableSlots;

    public DriverAvailability(String employeeId) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("employeeId must not be null or blank");
        }
        this.employeeId = employeeId;
        this.availableSlots = new EnumMap<>(DayOfWeek.class);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    /**
     * Sets the available shift types for a given day.
     * Passing a null or empty set removes all availability for that day.
     */
    public void setAvailableShifts(DayOfWeek day, Set<ShiftType> shifts) {
        Objects.requireNonNull(day, "Day of week must not be null");
        if (shifts == null || shifts.isEmpty()) {
            availableSlots.remove(day);
        } else {
            availableSlots.put(day, EnumSet.copyOf(shifts));
        }
    }

    /** Removes all recorded availability for a given day. */
    public void clearDay(DayOfWeek day) {
        Objects.requireNonNull(day, "Day of week must not be null");
        availableSlots.remove(day);
    }

    /**
     * Returns true if this driver is available to work the given shift type on the given day
     * according to the system record.
     */
    public boolean isAvailableFor(DayOfWeek day, ShiftType shiftType) {
        if (day == null || shiftType == null) {
            return false;
        }
        Set<ShiftType> shifts = availableSlots.get(day);
        return shifts != null && shifts.contains(shiftType);
    }

    /**
     * Returns the set of shift types the driver can work on a specific day,
     * or an empty set if no availability is recorded.
     */
    public Set<ShiftType> getAvailableShiftsForDay(DayOfWeek day) {
        Objects.requireNonNull(day, "Day of week must not be null");
        Set<ShiftType> shifts = availableSlots.get(day);
        return shifts == null ? Collections.emptySet() : Collections.unmodifiableSet(shifts);
    }

    /** Returns an unmodifiable view of all available slots. */
    public Map<DayOfWeek, Set<ShiftType>> getAvailableSlots() {
        Map<DayOfWeek, Set<ShiftType>> copy = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<DayOfWeek, Set<ShiftType>> entry : availableSlots.entrySet()) {
            copy.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public String toString() {
        return "DriverAvailability{employeeId='" + employeeId + "', availableSlots=" + availableSlots + "}";
    }
}
