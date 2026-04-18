package employees.domain;

import java.time.LocalDate;

/**
 * EmploymentTerms class represents the terms of employment for an employee.
 * It includes attributes such as start date, employment scope, global salary, hourly salary, and vacation days.
 */
public class EmploymentTerms {
    private LocalDate startDate;
    private EmploymentScope employmentScope;
    private double globalSalary;
    private double hourlySalary;
    private int vacationDays;

    public EmploymentTerms(
        LocalDate startDate,
        EmploymentScope employmentScope,
        double globalSalary,
        double hourlySalary,
        int vacationDays
    ) {
        this.startDate = startDate;
        this.employmentScope = employmentScope;
        this.globalSalary = globalSalary;
        this.hourlySalary = hourlySalary;
        this.vacationDays = vacationDays;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public EmploymentScope getEmploymentScope() {
        return employmentScope;
    }

    public void setEmploymentScope(EmploymentScope employmentScope) {
        this.employmentScope = employmentScope;
    }

    public double getGlobalSalary() {
        return globalSalary;
    }

    public void setGlobalSalary(double globalSalary) {
        this.globalSalary = globalSalary;
    }

    public double getHourlySalary() {
        return hourlySalary;
    }

    public void setHourlySalary(double hourlySalary) {
        this.hourlySalary = hourlySalary;
    }

    public int getVacationDays() {
        return vacationDays;
    }

    public void setVacationDays(int vacationDays) {
        if (vacationDays < 0) {
            throw new IllegalArgumentException("Vacation days cannot be negative");
        }
        this.vacationDays = vacationDays;
    }
}
