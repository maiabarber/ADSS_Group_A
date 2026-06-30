package dataaccess.mapper;

import dataaccess.dto.EmployeeDto;
import employee.domain.BankAccount;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentTerms;
import employee.domain.EmploymentType;
import employee.domain.Constraint;
import employee.domain.Preference;
import employee.domain.Role;
import employee.domain.Salary;
import employee.domain.ShiftType;
import employee.domain.WeeklyAvailabilityRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        BankAccount bankAccount = employee.getBankAccount();
        EmploymentTerms terms = employee.getEmploymentTerms();
        Salary salary = employee.getSalary();

        Integer branchId = null;
        Branch branch = employee.getBranch();
        if (branch != null) {
            try {
                branchId = Integer.parseInt(branch.getBranchId());
            } catch (NumberFormatException ignored) {
                branchId = null;
            }
        }

        return new EmployeeDto(
                employee.getId(),
                employee.getName(),
                bankAccount == null ? null : bankAccount.getBankNumber(),
                bankAccount == null ? null : bankAccount.getBranchNumber(),
                bankAccount == null ? null : bankAccount.getAccountNumber(),
                employee.getEmploymentType() == null ? null : employee.getEmploymentType().name(),
                terms == null || terms.getEmploymentScope() == null ? null : terms.getEmploymentScope().name(),
                salary == null ? 0 : salary.getHourlySalary(),
                salary == null ? 0 : salary.getGlobalSalary(),
                terms == null || terms.getStartDate() == null ? null : terms.getStartDate().toString(),
                employee.isFired(),
                terms == null ? 0 : terms.getVacationDays(),
                branchId,
                employee.canManageShift(),
                employee.getFixedDayOff() == null ? null : employee.getFixedDayOff().name(),
                employee.getWeeklyAvailabilityRequest() == null
                        || employee.getWeeklyAvailabilityRequest().getWeekStartDate() == null
                        ? null
                        : employee.getWeeklyAvailabilityRequest().getWeekStartDate().toString(),
                employee.getWeeklyAvailabilityRequest() == null
                        || employee.getWeeklyAvailabilityRequest().getSubmissionDeadline() == null
                        ? null
                        : employee.getWeeklyAvailabilityRequest().getSubmissionDeadline().toString(),
                serializeConstraints(employee.getWeeklyAvailabilityRequest()),
                serializePreferences(employee.getWeeklyAvailabilityRequest())
        );
    }

    public static Employee toDomain(EmployeeDto dto, String password, Set<Role> roles, Branch branch) {
        if (dto == null) {
            return null;
        }

        EmploymentScope scope = dto.getEmploymentScope() == null || dto.getEmploymentScope().isBlank()
                ? EmploymentScope.FULL_TIME
                : EmploymentScope.valueOf(dto.getEmploymentScope());

        LocalDate startDate = dto.getStartDate() == null || dto.getStartDate().isBlank()
                ? LocalDate.now()
                : LocalDate.parse(dto.getStartDate());

        EmploymentTerms terms = new EmploymentTerms(
                startDate,
                scope,
                dto.getGlobalSalary(),
                dto.getHourlySalary(),
                dto.getVacationDays()
        );

        Salary salary = new Salary(dto.getGlobalSalary(), dto.getHourlySalary(), 0, scope);

        EmploymentType type = dto.getEmploymentType() == null || dto.getEmploymentType().isBlank()
                ? EmploymentType.REGULAR
                : EmploymentType.valueOf(dto.getEmploymentType());

        return new Employee(
                dto.getEmployeeId(),
                password == null ? "1234" : password,
                new BankAccount(
                        defaultString(dto.getBankNumber(), "1"),
                        defaultString(dto.getBankBranchNumber(), "1"),
                        defaultString(dto.getBankAccountNumber(), "1")
                ),
                dto.getName(),
                salary,
                type,
                terms,
                roles == null ? new HashSet<>() : roles,
                dto.canManageShift(),
                dto.isFired(),
                parseDayOfWeek(dto.getFixedDayOff()),
                parseWeeklyAvailabilityRequest(dto),
                branch
        );
    }

    private static String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private static String serializeConstraints(WeeklyAvailabilityRequest request) {
        if (request == null || request.getConstraints().isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        for (Constraint constraint : request.getConstraints()) {
            if (constraint.getDay() != null && constraint.getShiftType() != null) {
                values.add(constraint.getDay().name() + ":" + constraint.getShiftType().name());
            }
        }
        return values.isEmpty() ? null : String.join(";", values);
    }

    private static String serializePreferences(WeeklyAvailabilityRequest request) {
        if (request == null || request.getPreferences().isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        for (Preference preference : request.getPreferences()) {
            if (preference.getDay() != null && preference.getShiftType() != null) {
                values.add(preference.getDay().name() + ":" + preference.getShiftType().name());
            }
        }
        return values.isEmpty() ? null : String.join(";", values);
    }

    private static DayOfWeek parseDayOfWeek(String value) {
        return value == null || value.isBlank() ? null : DayOfWeek.valueOf(value);
    }

    private static WeeklyAvailabilityRequest parseWeeklyAvailabilityRequest(EmployeeDto dto) {
        boolean hasRequest = hasText(dto.getWeeklyWeekStartDate())
                || hasText(dto.getWeeklySubmissionDeadline())
                || hasText(dto.getWeeklyConstraints())
                || hasText(dto.getWeeklyPreferences());
        if (!hasRequest) {
            return null;
        }

        WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();
        if (hasText(dto.getWeeklyWeekStartDate())) {
            request.setWeekStartDate(LocalDate.parse(dto.getWeeklyWeekStartDate()));
        }
        if (hasText(dto.getWeeklySubmissionDeadline())) {
            request.setSubmissionDeadline(LocalDate.parse(dto.getWeeklySubmissionDeadline()));
        }
        request.setConstraints(parseConstraints(dto.getWeeklyConstraints()));
        request.setPreferences(parsePreferences(dto.getWeeklyPreferences()));
        return request;
    }

    private static List<Constraint> parseConstraints(String serialized) {
        List<Constraint> constraints = new ArrayList<>();
        for (String value : splitSerializedAvailability(serialized)) {
            String[] parts = value.split(":", 2);
            if (parts.length == 2) {
                constraints.add(new Constraint(DayOfWeek.valueOf(parts[0]), ShiftType.valueOf(parts[1])));
            }
        }
        return constraints;
    }

    private static List<Preference> parsePreferences(String serialized) {
        List<Preference> preferences = new ArrayList<>();
        for (String value : splitSerializedAvailability(serialized)) {
            String[] parts = value.split(":", 2);
            if (parts.length == 2) {
                preferences.add(new Preference(DayOfWeek.valueOf(parts[0]), ShiftType.valueOf(parts[1])));
            }
        }
        return preferences;
    }

    private static String[] splitSerializedAvailability(String serialized) {
        return hasText(serialized) ? serialized.split(";") : new String[0];
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
