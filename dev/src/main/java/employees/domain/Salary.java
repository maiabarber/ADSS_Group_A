package employees.domain;

/**
 * Represents a salary computation for an employee.
 *
 * globalSalary is the base salary for a full-time employee,
 * hourlySalary is the rate used for overtime hours,
 * workedHours is the total hours worked, and finalSalary is
 * the computed total salary including overtime pay.
 */
public class Salary {
    private double globalSalary;
    private double hourlySalary;
    private double workedHours;
    private double overtimeHours;
    private double finalSalary;

    public static final int FULL_TIME_HOURS = 190;

    public Salary() {
        recalculateFinalSalary();
    }

    public Salary(double globalSalary, double hourlySalary, double workedHours) {
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.workedHours = workedHours;
        recalculateFinalSalary();
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public void setGlobalSalary(double globalSalary) {
        this.globalSalary = globalSalary;
        recalculateFinalSalary();
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public void setHourlySalary(double hourlySalary) {
        this.hourlySalary = hourlySalary;
        recalculateFinalSalary();
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(double workedHours) {
        this.workedHours = workedHours;
        recalculateFinalSalary();
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    /**
     * Calculates overtime hours based on the configured full-time threshold.
     */
    public double calculateOvertimeHours() {
        return Math.max(0, workedHours - FULL_TIME_HOURS);
    }

    /**
     * Recomputes overtimeHours and finalSalary from the current values.
     */
    public double recalculateFinalSalary() {
        overtimeHours = calculateOvertimeHours();
        finalSalary = globalSalary + (overtimeHours * hourlySalary);
        return finalSalary;
    }

    @Override
    public String toString() {
        return String.format(
            "Salary{globalSalary=%.2f, hourlySalary=%.2f, workedHours=%.2f, overtimeHours=%.2f, finalSalary=%.2f}",
            globalSalary,
            hourlySalary,
            workedHours,
            overtimeHours,
            finalSalary
        );
    }
}
