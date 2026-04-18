package employees.domain;

/**
 * Salary class represents the salary details of an employee.
 * It includes the global salary, hourly salary, worked hours, overtime hours, and the final calculated salary.
 */
public class Salary {
    private double globalSalary;
    private double hourlySalary;
    private double workedHours;
    private double overtimeHours;
    private double finalSalary;
    private EmploymentScope employmentScope = EmploymentScope.FULL_TIME;
    public static final int FULL_TIME_HOURS = 190;
    public static final int PART_TIME_HOURS = 95;



    public Salary(double globalSalary, double hourlySalary, double workedHours) {
        this(globalSalary, hourlySalary, workedHours, EmploymentScope.FULL_TIME);
    }

    public Salary(double globalSalary, double hourlySalary, double workedHours, EmploymentScope employmentScope) {
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.workedHours = workedHours;
        this.employmentScope = employmentScope;
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

    public EmploymentScope getEmploymentScope() {
        return employmentScope;
    }

    public void setEmploymentScope(EmploymentScope employmentScope) {
        this.employmentScope = employmentScope;
        recalculateFinalSalary();
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    public double calculateOvertimeHours() {
        int threshold = (employmentScope == EmploymentScope.PART_TIME) ? PART_TIME_HOURS : FULL_TIME_HOURS;
        return Math.max(0, workedHours - threshold);
    }

    public double calculateOvertimeHours(EmploymentScope scope) {
        int threshold = (scope == EmploymentScope.PART_TIME) ? PART_TIME_HOURS : FULL_TIME_HOURS;
        return Math.max(0, workedHours - threshold);
    }

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
