package employee.service;

import employee.domain.Constraint;
import employee.domain.Employee;
import employee.domain.Preference;
import employee.domain.SubmissionDeadlinePolicy;
import employee.domain.WeeklyAvailabilityRules;
import dataaccess.repository.EmployeeRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import dataaccess.repository.RepositoryException;
import dataaccess.repository.SubmissionDeadlineRepository;

/**
 * WeeklyAvailabilityService coordinates repositories with domain rules for weekly submission.
 */
public class WeeklyAvailabilityService {
    private final SubmissionDeadlineRepository submissionDeadlineRepository;
    private final EmployeeRepository employeeRepository;
    private final SubmissionDeadlinePolicy deadlinePolicy;
    private final WeeklyAvailabilityRules weeklyAvailabilityRules;

    public WeeklyAvailabilityService(
        SubmissionDeadlineRepository submissionDeadlineRepository,
        EmployeeRepository employeeRepository
    ) {
        this.submissionDeadlineRepository = submissionDeadlineRepository;
        this.employeeRepository = employeeRepository;
        this.deadlinePolicy = new SubmissionDeadlinePolicy();
        this.weeklyAvailabilityRules = new WeeklyAvailabilityRules();
    }

    public int submitWeeklyAvailability(
        Employee employee,
        List<Constraint> constraints,
        List<Preference> preferences,
        int vacationDaysToUse,
        List<DayOfWeek> selectedVacationDays,
        LocalDate today
    ) throws RepositoryException {
        LocalDate configuredDeadline = submissionDeadlineRepository.findCurrent().orElse(null);
        LocalDate activeDeadline = deadlinePolicy.resolveOpenSubmissionDeadline(configuredDeadline, today);

        int remainingBalance = employee.submitWeeklyAvailability(
            constraints,
            preferences,
            vacationDaysToUse,
            selectedVacationDays,
            activeDeadline,
            weeklyAvailabilityRules
        );

        employeeRepository.save(employee);
        return remainingBalance;
    }
}
