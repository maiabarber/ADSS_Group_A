package employees.service;

import employees.domain.Constraint;
import employees.domain.Employee;
import employees.domain.Preference;
import employees.domain.SubmissionDeadlinePolicy;
import employees.domain.WeeklyAvailabilityRequest;
import employees.domain.WeeklyAvailabilityRules;
import employees.repository.EmployeeRepository;
import employees.repository.RepositoryException;
import employees.repository.SubmissionDeadlineRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

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
        if (employee == null) {
            throw new IllegalArgumentException("Employee is required");
        }

        LocalDate configuredDeadline = submissionDeadlineRepository.findCurrent().orElse(null);
        LocalDate activeDeadline = deadlinePolicy.resolveOpenSubmissionDeadline(configuredDeadline, today);

        weeklyAvailabilityRules.validateVacationUsage(employee, vacationDaysToUse, selectedVacationDays);
        List<Constraint> mergedConstraints = weeklyAvailabilityRules.mergeConstraintsWithVacationDays(
            constraints,
            selectedVacationDays
        );

        employee.consumeVacationDays(vacationDaysToUse);

        WeeklyAvailabilityRequest request = employee.getWeeklyAvailabilityRequest();
        if (request == null) {
            request = new WeeklyAvailabilityRequest();
            employee.setWeeklyAvailabilityRequest(request);
        }

        request.setConstraints(mergedConstraints);
        request.setPreferences(preferences);
        request.setSubmissionDeadline(activeDeadline);

        employeeRepository.save(employee);
        return employee.getVacationDaysBalance();
    }
}
