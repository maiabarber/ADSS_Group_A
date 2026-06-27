package dataaccess.dto;

import employee.domain.EmploymentScope;

public final class SalaryDto {
    private final double globalSalary;
    private final double hourlySalary;
    private final double workedHours;
    private final double overtimeHours;
    private final double finalSalary;
    private final EmploymentScope employmentScope;

    public SalaryDto(double globalSalary, double hourlySalary, double workedHours, double overtimeHours, double finalSalary, EmploymentScope employmentScope) {
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.workedHours = workedHours;
        this.overtimeHours = overtimeHours;
        this.finalSalary = finalSalary;
        this.employmentScope = employmentScope;
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public double getWorkedHours() {
        return workedHours;
    }

    public double getOvertimeHours() {
        return overtimeHours;
    }

    public double getFinalSalary() {
        return finalSalary;
    }

    public EmploymentScope getEmploymentScope() {
        return employmentScope;
    }

}