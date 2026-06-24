package employee.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import employee.domain.BankAccount;
import employee.domain.Constraint;
import employee.domain.Branch;
import employee.domain.Employee;
import employee.domain.EmploymentScope;
import employee.domain.EmploymentTerms;
import employee.domain.EmploymentType;
import employee.domain.HR_Manager;
import employee.domain.Preference;
import employee.domain.Role;
import employee.domain.Salary;
import employee.domain.Shift;
import employee.domain.ShiftAssignment;
import employee.domain.ShiftType;
import employee.domain.User;
import employee.domain.WeeklyAvailabilityRequest;
import employee.domain.WeeklyAvailabilityRules;
import employee.presentation.ConsolePresentation;
import employee.presentation.ShiftController;
import employee.presentation.UserController;
import employee.repository.EmployeeRepository;
import employee.repository.RepositoryException;
import employee.repository.impl.InMemoryEmployeeRepository;
import employee.domain.SubmissionDeadlinePolicy;
import employee.repository.impl.InMemorySubmissionDeadlineRepository;
import employee.repository.impl.InMemoryUserRepository;
import employee.service.AuthenticationService;
import employee.service.WeeklyAvailabilityService;
import transportation.domain.Site;
import transportation.domain.ShippingZone;
import transportation.domain.SiteType;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Tests {
	private static final Branch DEFAULT_TEST_BRANCH = new Branch("B-DEFAULT", "Default Test Branch", "Default Location");

	@Test
	public void easy_salaryNoOvertime_whenWorkedBelowThreshold() {
		Salary salary = new Salary(5000, 50, 120, EmploymentScope.FULL_TIME);

		assertEquals(0.0, salary.getOvertimeHours(), 0.0001, "Overtime should be zero for full-time under 190h");
		assertEquals(5000.0, salary.getFinalSalary(), 0.0001, "Final salary should equal base when no overtime");
	}

	@Test
	public void easy_salaryOvertime_forPartTimeEmployee() {
		Salary salary = new Salary(3000, 40, 100, EmploymentScope.PART_TIME);

		assertEquals(5.0, salary.getOvertimeHours(), 0.0001, "Part-time overtime should be workedHours - 95");
		assertEquals(3200.0, salary.getFinalSalary(), 0.0001, "Final salary should include overtime pay");
	}

	@Test
	public void easy_salaryOvertime_forFullTimeEmployee() {
		Salary salary = new Salary(5000, 50, 200, EmploymentScope.FULL_TIME);

		assertEquals(10.0, salary.getOvertimeHours(), 0.0001, "Full-time overtime should be workedHours - 190");
		assertEquals(5500.0, salary.getFinalSalary(), 0.0001, "Final salary should be global salary plus overtime pay");
	}

	@Test
	public void easy_salaryNoOvertime_atExactThreshold() {
		Salary fullTime = new Salary(5000, 50, 190, EmploymentScope.FULL_TIME);
		Salary partTime = new Salary(3000, 40, 95, EmploymentScope.PART_TIME);

		assertEquals(0.0, fullTime.getOvertimeHours(), 0.0001, "Full-time overtime should be zero at exactly 190h");
		assertEquals(0.0, partTime.getOvertimeHours(), 0.0001, "Part-time overtime should be zero at exactly 95h");
	}

	@Test
	public void easy_salaryRejectsNullEmploymentScope() {
		Salary salary = new Salary(3000, 30, 100);

		assertThrows(IllegalArgumentException.class, () -> salary.setEmploymentScope(null));
	}

	@Test
	public void easy_weeklyAvailabilityReset_clearsData() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();
		request.addConstraint(new employee.domain.Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		request.addPreference(new employee.domain.Preference(DayOfWeek.MONDAY, ShiftType.EVENING));

		LocalDate newWeek = LocalDate.of(2026, 4, 20);
		request.resetForWeek(newWeek);

		assertTrue("Constraints should be cleared after reset", request.getConstraints().isEmpty());
		assertTrue("Preferences should be cleared after reset", request.getPreferences().isEmpty());
		assertEquals(newWeek, request.getWeekStartDate(), "Week start date should be updated");
	}

	@Test
	public void easy_weeklyAvailability_employeeCanResubmitEachWeek() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

		// Week 1 submission
		request.addConstraint(new Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		request.addPreference(new Preference(DayOfWeek.MONDAY, ShiftType.EVENING));
		assertEquals(1, request.getConstraints().size(), "Week 1 should have 1 constraint");
		assertEquals(1, request.getPreferences().size(), "Week 1 should have 1 preference");

		// Reset for week 2
		request.resetForWeek(LocalDate.of(2026, 7, 27));
		assertTrue("Constraints should be cleared for week 2", request.getConstraints().isEmpty());
		assertTrue("Preferences should be cleared for week 2", request.getPreferences().isEmpty());

		// Week 2 re-submission with different data
		request.addConstraint(new Constraint(DayOfWeek.WEDNESDAY, ShiftType.EVENING));
		request.addPreference(new Preference(DayOfWeek.THURSDAY, ShiftType.MORNING));
		assertEquals(1, request.getConstraints().size(), "Week 2 should have 1 newly submitted constraint");
		assertEquals(DayOfWeek.WEDNESDAY, request.getConstraints().get(0).getDay(), "Week 2 constraint day should be WEDNESDAY");
		assertEquals(1, request.getPreferences().size(), "Week 2 should have 1 newly submitted preference");
		assertEquals(DayOfWeek.THURSDAY, request.getPreferences().get(0).getDay(), "Week 2 preference day should be THURSDAY");
	}

	@Test
	public void hard_weeklyAvailabilityReset_forAllEmployeesAtNewWeek() throws Exception {
		ConsolePresentation console = new ConsolePresentation();
		Employee first = buildEmployee("100000171", false, 10);
		Employee second = buildEmployee("100000172", false, 10);

		LocalDate previousWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
		first.getWeeklyAvailabilityRequest().resetForWeek(previousWeekStart);
		first.getWeeklyAvailabilityRequest().addConstraint(new Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		first.getWeeklyAvailabilityRequest().addPreference(new Preference(DayOfWeek.MONDAY, ShiftType.EVENING));
		second.getWeeklyAvailabilityRequest().resetForWeek(previousWeekStart);
		second.getWeeklyAvailabilityRequest().addConstraint(new Constraint(DayOfWeek.TUESDAY, ShiftType.MORNING));
		second.getWeeklyAvailabilityRequest().addPreference(new Preference(DayOfWeek.WEDNESDAY, ShiftType.EVENING));

		java.lang.reflect.Method ensureWeeklyAvailabilityCurrent = ConsolePresentation.class
			.getDeclaredMethod("ensureWeeklyAvailabilityCurrent", Employee.class);
		ensureWeeklyAvailabilityCurrent.setAccessible(true);
		ensureWeeklyAvailabilityCurrent.invoke(console, first);
		ensureWeeklyAvailabilityCurrent.invoke(console, second);

		LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		assertTrue("First employee constraints should reset at week start", first.getWeeklyAvailabilityRequest().getConstraints().isEmpty());
		assertTrue("First employee preferences should reset at week start", first.getWeeklyAvailabilityRequest().getPreferences().isEmpty());
		assertEquals(currentWeekStart, first.getWeeklyAvailabilityRequest().getWeekStartDate(), "First employee week should advance to current week");
		assertTrue("Second employee constraints should reset at week start", second.getWeeklyAvailabilityRequest().getConstraints().isEmpty());
		assertTrue("Second employee preferences should reset at week start", second.getWeeklyAvailabilityRequest().getPreferences().isEmpty());
		assertEquals(currentWeekStart, second.getWeeklyAvailabilityRequest().getWeekStartDate(), "Second employee week should advance to current week");
	}

	@Test
	public void hard_weeklyAvailability_vacationDayConstraintConsumesBalance() {
		InMemorySubmissionDeadlineRepository deadlineRepository = new InMemorySubmissionDeadlineRepository();
		InMemoryEmployeeRepository employeeRepository = new InMemoryEmployeeRepository();
		WeeklyAvailabilityService service = new WeeklyAvailabilityService(deadlineRepository, employeeRepository);
		Employee employee = buildEmployee("100000173", false, 10);

		try {
			employeeRepository.save(employee);
			deadlineRepository.save(LocalDate.of(2026, 7, 25));
			List<Constraint> constraints = new ArrayList<>();
			constraints.add(new Constraint(DayOfWeek.MONDAY, ShiftType.MORNING));
			List<Preference> preferences = Collections.singletonList(new Preference(DayOfWeek.FRIDAY, ShiftType.EVENING));
			List<DayOfWeek> selectedVacationDays = Collections.singletonList(DayOfWeek.TUESDAY);

			int remainingDays = service.submitWeeklyAvailability(
				employee,
				constraints,
				preferences,
				1,
				selectedVacationDays,
				LocalDate.of(2026, 7, 20)
			);

			WeeklyAvailabilityRequest request = employee.getWeeklyAvailabilityRequest();
			assertEquals(9, remainingDays, "Using one vacation day should reduce balance by one");
			assertEquals(9, employee.getVacationDaysBalance(), "Employee vacation balance should be reduced");
			assertEquals(5, request.getConstraints().size(), "Vacation day should add full-day constraints plus existing one");

			int tuesdayConstraints = 0;
			boolean hasMorning = false;
			boolean hasMorningOvertime = false;
			boolean hasEvening = false;
			boolean hasDoubleShift = false;
			for (Constraint c : request.getConstraints()) {
				if (c.getDay() == DayOfWeek.TUESDAY) {
					tuesdayConstraints++;
					hasMorning = hasMorning || c.getShiftType() == ShiftType.MORNING;
					hasMorningOvertime = hasMorningOvertime || c.getShiftType() == ShiftType.MORNING_OVERTIME;
					hasEvening = hasEvening || c.getShiftType() == ShiftType.EVENING;
					hasDoubleShift = hasDoubleShift || c.getShiftType() == ShiftType.DOUBLE_SHIFT;
				}
			}

			assertEquals(4, tuesdayConstraints, "Vacation day should map to exactly four shift constraints");
			assertTrue("Vacation day should block MORNING", hasMorning);
			assertTrue("Vacation day should block MORNING_OVERTIME", hasMorningOvertime);
			assertTrue("Vacation day should block EVENING", hasEvening);
			assertTrue("Vacation day should block DOUBLE_SHIFT", hasDoubleShift);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_weeklyAvailability_submitRejectedWithoutDeadline_keepsVacationBalance() {
		InMemorySubmissionDeadlineRepository deadlineRepository = new InMemorySubmissionDeadlineRepository();
		InMemoryEmployeeRepository employeeRepository = new InMemoryEmployeeRepository();
		WeeklyAvailabilityService service = new WeeklyAvailabilityService(deadlineRepository, employeeRepository);
		Employee employee = buildEmployee("100000178", false, 10);

		try {
			employeeRepository.save(employee);
			List<Constraint> constraints = Collections.singletonList(new Constraint(DayOfWeek.MONDAY, ShiftType.MORNING));
			List<Preference> preferences = Collections.singletonList(new Preference(DayOfWeek.TUESDAY, ShiftType.EVENING));
			List<DayOfWeek> selectedVacationDays = Collections.singletonList(DayOfWeek.WEDNESDAY);

			assertThrows(
				IllegalArgumentException.class,
				() -> {
					try {
						service.submitWeeklyAvailability(
							employee,
							constraints,
							preferences,
							1,
							selectedVacationDays,
							LocalDate.of(2026, 4, 20)
						);
					} catch (RepositoryException e) {
						throw new RuntimeException(e);
					}
				}
			);

			assertEquals(10, employee.getVacationDaysBalance(), "Failed submission should not consume vacation days");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_weeklyAvailability_currentWeekDoesNotResetExistingData() throws Exception {
		ConsolePresentation console = new ConsolePresentation();
		Employee employee = buildEmployee("100000179", false, 10);
		LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

		employee.getWeeklyAvailabilityRequest().resetForWeek(currentWeekStart);
		employee.getWeeklyAvailabilityRequest().addConstraint(new Constraint(DayOfWeek.MONDAY, ShiftType.MORNING));
		employee.getWeeklyAvailabilityRequest().addPreference(new Preference(DayOfWeek.TUESDAY, ShiftType.EVENING));

		java.lang.reflect.Method ensureWeeklyAvailabilityCurrent = ConsolePresentation.class
			.getDeclaredMethod("ensureWeeklyAvailabilityCurrent", Employee.class);
		ensureWeeklyAvailabilityCurrent.setAccessible(true);
		ensureWeeklyAvailabilityCurrent.invoke(console, employee);

		assertEquals(1, employee.getWeeklyAvailabilityRequest().getConstraints().size(), "Current-week request should keep existing constraints");
		assertEquals(1, employee.getWeeklyAvailabilityRequest().getPreferences().size(), "Current-week request should keep existing preferences");
		assertEquals(currentWeekStart, employee.getWeeklyAvailabilityRequest().getWeekStartDate(), "Week start should remain current week");
	}

	@Test
	public void hard_weeklyAvailabilityRules_vacationMerge_doesNotDuplicateExistingConstraint() {
		WeeklyAvailabilityRules rules = new WeeklyAvailabilityRules();
		List<Constraint> constraints = new ArrayList<>();
		constraints.add(new Constraint(DayOfWeek.TUESDAY, ShiftType.MORNING));

		List<Constraint> merged = rules.mergeConstraintsWithVacationDays(
			constraints,
			Collections.singletonList(DayOfWeek.TUESDAY)
		);

		assertEquals(4, merged.size(), "Vacation merge should produce exactly four shift constraints for the vacation day");

		int morningCount = 0;
		for (Constraint c : merged) {
			if (c.getDay() == DayOfWeek.TUESDAY && c.getShiftType() == ShiftType.MORNING) {
				morningCount++;
			}
		}
		assertEquals(1, morningCount, "Existing MORNING constraint should not be duplicated");
	}

	@Test
	public void hard_addEmployee_setsDefaultVacationDaysToTenAtHiring() {
		AuthenticationService authenticationService = new AuthenticationService(new InMemoryUserRepository());
		InMemoryEmployeeRepository employeeRepository = new InMemoryEmployeeRepository();
		UserController controller = new UserController(authenticationService, employeeRepository);
		HR_Manager hr = new HR_Manager("100000174", "pass");
		Employee newHire = buildEmployee("100000175", false, 2);

		try {
			controller.addEmployee(hr, newHire);
			assertEquals(10, newHire.getVacationDaysBalance(), "Newly hired employee should start with 10 vacation days");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_yearlyVacationReset_setsAllEmployeesToTen() throws Exception {
		ConsolePresentation console = new ConsolePresentation();
		java.lang.reflect.Field employeeRepositoryField = ConsolePresentation.class.getDeclaredField("employeeRepository");
		employeeRepositoryField.setAccessible(true);
		EmployeeRepository employeeRepository = (EmployeeRepository) employeeRepositoryField.get(console);

		Employee first = buildEmployee("100000176", false, 3);
		Employee second = buildEmployee("100000177", false, 8);
		employeeRepository.save(first);
		employeeRepository.save(second);

		java.lang.reflect.Method resetVacationDaysForCurrentYear = ConsolePresentation.class
			.getDeclaredMethod("resetVacationDaysForCurrentYear");
		resetVacationDaysForCurrentYear.setAccessible(true);
		resetVacationDaysForCurrentYear.invoke(console);

		Employee firstAfterReset = employeeRepository.findById("100000176").orElse(null);
		Employee secondAfterReset = employeeRepository.findById("100000177").orElse(null);
		assertEquals(10, firstAfterReset.getVacationDaysBalance(), "Annual reset should set first employee to 10 vacation days");
		assertEquals(10, secondAfterReset.getVacationDaysBalance(), "Annual reset should set second employee to 10 vacation days");
	}

	@Test
	public void hard_yearlyVacationReset_secondCallSameYear_isNoOp() throws Exception {
		ConsolePresentation console = new ConsolePresentation();
		java.lang.reflect.Field employeeRepositoryField = ConsolePresentation.class.getDeclaredField("employeeRepository");
		employeeRepositoryField.setAccessible(true);
		EmployeeRepository employeeRepository = (EmployeeRepository) employeeRepositoryField.get(console);

		Employee employee = buildEmployee("100000180", false, 2);
		employeeRepository.save(employee);

		java.lang.reflect.Method resetVacationDaysForCurrentYear = ConsolePresentation.class
			.getDeclaredMethod("resetVacationDaysForCurrentYear");
		resetVacationDaysForCurrentYear.setAccessible(true);
		resetVacationDaysForCurrentYear.invoke(console);

		Employee afterFirstReset = employeeRepository.findById("100000180").orElse(null);
		afterFirstReset.resetVacationDays(4);
		employeeRepository.save(afterFirstReset);

		resetVacationDaysForCurrentYear.invoke(console);

		Employee afterSecondCall = employeeRepository.findById("100000180").orElse(null);
		assertEquals(4, afterSecondCall.getVacationDaysBalance(), "Second annual reset call in same year should not run again");
	}

	@Test
	public void easy_weeklyAvailability_constraintStoredAndRetrievable() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

		request.addConstraint(new Constraint(DayOfWeek.TUESDAY, ShiftType.MORNING));

		assertEquals(1, request.getConstraints().size(), "Constraint list should contain exactly one entry");
		assertEquals(DayOfWeek.TUESDAY, request.getConstraints().get(0).getDay(), "Stored constraint day should be TUESDAY");
		assertEquals(ShiftType.MORNING, request.getConstraints().get(0).getShiftType(), "Stored constraint shift type should be MORNING");
	}

	@Test
	public void easy_weeklyAvailability_preferenceStoredAndRetrievable() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

		request.addPreference(new Preference(DayOfWeek.WEDNESDAY, ShiftType.EVENING));

		assertEquals(1, request.getPreferences().size(), "Preference list should contain exactly one entry");
		assertEquals(DayOfWeek.WEDNESDAY, request.getPreferences().get(0).getDay(), "Stored preference day should be WEDNESDAY");
		assertEquals(ShiftType.EVENING, request.getPreferences().get(0).getShiftType(), "Stored preference shift type should be EVENING");
	}

	@Test
	public void easy_weeklyAvailability_multipleConstraintsStored() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

		request.addConstraint(new Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		request.addConstraint(new Constraint(DayOfWeek.MONDAY, ShiftType.EVENING));

		assertEquals(2, request.getConstraints().size(), "All added constraints should be present in the record");
	}

	@Test
	public void easy_weeklyAvailability_morningPreferenceStoredAndRetrievable() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();

		request.addPreference(new Preference(DayOfWeek.THURSDAY, ShiftType.MORNING));

		assertEquals(1, request.getPreferences().size(), "Preference list should contain exactly one entry");
		assertEquals(DayOfWeek.THURSDAY, request.getPreferences().get(0).getDay(), "Stored preference day should be THURSDAY");
		assertEquals(ShiftType.MORNING, request.getPreferences().get(0).getShiftType(), "Stored preference shift type should be MORNING");
	}

	@Test
	public void easy_shiftAssignmentPendingLogic() {
		ShiftAssignment assignment = new ShiftAssignment(null, null, Role.CASHIER, true);

		assertTrue("Assignment requiring approval should start as pending", assignment.isPending());

		assignment.setApproved(true);
		assertFalse("Approved assignment should no longer be pending", assignment.isPending());
	}

	@Test
	public void hard_employeeVacationConsumption_reducesBalance() {
		Employee employee = buildEmployee("100000001", false, 10);

		employee.consumeVacationDays(3);

		assertEquals(7, employee.getVacationDaysBalance(), "Vacation balance should decrease by consumed amount");
	}

	@Test
	public void hard_employeeVacationConsumption_rejectsOveruse() {
		Employee employee = buildEmployee("100000002", false, 2);

		assertThrows(IllegalArgumentException.class, () -> employee.consumeVacationDays(3));
	}

	@Test
	public void hard_employeeFixedDayOff_canOnlyBeSetOnce() {
		Employee employee = buildEmployee("100000003", false, 10);

		employee.setFixedDayOff(DayOfWeek.SUNDAY);
		assertEquals(DayOfWeek.SUNDAY, employee.getFixedDayOff(), "Fixed day off should be set the first time");

		assertThrows(IllegalStateException.class, () -> employee.setFixedDayOff(DayOfWeek.MONDAY));
	}

	@Test
	public void hard_employeeFixedDayOff_canBeChosenAtHiring() {
		Salary salary = new Salary(5000, 50, 0, EmploymentScope.FULL_TIME);
		EmploymentTerms terms = new EmploymentTerms(
			LocalDate.of(2026, 4, 21),
			EmploymentScope.FULL_TIME,
			salary.getGlobalSalary(),
			salary.getHourlySalary(),
			10
		);

		Employee employee = new Employee(
			"100000004",
			"pass",
			new BankAccount("10", "123", "000111"),
			"Emp 100000004",
			salary,
			EmploymentType.REGULAR,
			terms,
			Collections.singleton(Role.CASHIER),
			false,
			false,
			DayOfWeek.THURSDAY,
			new WeeklyAvailabilityRequest(),
			DEFAULT_TEST_BRANCH
		);

		assertEquals(DayOfWeek.THURSDAY, employee.getFixedDayOff(), "Fixed day off chosen at hiring should be stored");
	}

	@Test
	public void hard_shiftAssignManager_requiresHrManager() {
		Employee candidate = buildEmployee("100000011", true, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new employee.domain.User("100000021", "p"), candidate));
	}

	@Test
	public void hard_shiftAssignManager_requiresCertifiedCandidate() {
		Employee notCertified = buildEmployee("100000012", false, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new HR_Manager("100000071", "pass"), notCertified));
	}

	@Test
	public void hard_shiftAssignManager_success() {
		HR_Manager hr = new HR_Manager("100000072", "pass");
		Employee qualified = buildEmployee("100000013", true, 10);
		Shift shift = new Shift();

		shift.assignShiftManager(hr, qualified);

		assertEquals(qualified, shift.getShiftManager(), "Shift manager should be the assigned qualified employee");
	}

	@Test
	public void hard_shiftAssignManager_reassignKeepsSingleManager() {
		HR_Manager hr = new HR_Manager("100000160", "pass");
		Employee firstManager = buildEmployee("100000161", true, 10);
		Employee secondManager = buildEmployee("100000162", true, 10);
		Shift shift = new Shift();

		shift.assignShiftManager(hr, firstManager);
		assertEquals(firstManager, shift.getShiftManager(), "First assigned manager should be set");

		shift.assignShiftManager(hr, secondManager);

		assertEquals(secondManager, shift.getShiftManager(), "Reassignment should replace previous manager so shift has only one manager");
		assertFalse("Shift manager should no longer be the first manager after reassignment", shift.getShiftManager().equals(firstManager));
	}

	@Test
	public void hard_shiftAssignManager_rejectsFiredCandidate() {
		HR_Manager hr = new HR_Manager("100000073", "pass");
		Employee fired = buildEmployee("100000014", true, 10);
		fired.setFired(true);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(hr, fired));
	}

	@Test
	public void hard_authorizeShiftManager_grantsPermission() {
		Employee employee = buildEmployee("100000015", false, 10);

		assertFalse("Employee should not be authorized as shift manager initially", employee.canManageShift());

		employee.setCanManageShift(true);

		assertTrue("Employee should be authorized as shift manager after setCanManageShift(true)", employee.canManageShift());
	}

	@Test
	public void hard_authorizeShiftManager_allowsSubsequentAssignment() {
		HR_Manager hr = new HR_Manager("100000076", "pass");
		Employee employee = buildEmployee("100000016", false, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(hr, employee));

		employee.setCanManageShift(true);
		shift.assignShiftManager(hr, employee);

		assertEquals(employee, shift.getShiftManager(), "Employee authorized as shift manager should be assignable to a shift");
	}

	@Test
	public void hard_shiftConfigureRoleCounts_requiresHrManager() {
		Shift shift = new Shift();
		Map<Role, Integer> customCounts = new EnumMap<>(Role.class);
		customCounts.put(Role.CASHIER, 3);

		assertThrows(IllegalArgumentException.class, () -> shift.configureRequiredRoleCounts(new employee.domain.User("100000022", "p"), customCounts));
	}

	@Test
	public void hard_shiftConfigureRoleCounts_success() {
		HR_Manager hr = new HR_Manager("100000074", "pass");
		Shift shift = new Shift();
		Map<Role, Integer> customCounts = new EnumMap<>(Role.class);
		customCounts.put(Role.CASHIER, 3);
		customCounts.put(Role.STOREKEEPER, 2);

		shift.configureRequiredRoleCounts(hr, customCounts);

		assertEquals(3, shift.getRequiredRoleCounts().get(Role.CASHIER).intValue(), "Required cashiers should be updated to 3");
		assertEquals(2, shift.getRequiredRoleCounts().get(Role.STOREKEEPER).intValue(), "Required storekeepers should be updated to 2");
	}

	@Test
	public void hard_shiftConfigureRoleCounts_minimumOneEnforced() {
		HR_Manager hr = new HR_Manager("100000075", "pass");
		Shift shift = new Shift();
		Map<Role, Integer> invalidCounts = new EnumMap<>(Role.class);
		invalidCounts.put(Role.CASHIER, 0);

		assertThrows(IllegalArgumentException.class, () -> shift.configureRequiredRoleCounts(hr, invalidCounts));
	}

	@Test
	public void hard_shiftConfigureRoleCounts_defaultsAtLeastOne() {
		Shift shift = new Shift();

		for (Map.Entry<Role, Integer> entry : shift.getRequiredRoleCounts().entrySet()) {
			assertTrue("Default required count for role " + entry.getKey() + " should be at least 1", entry.getValue() >= 1);
		}
	}

	@Test
	public void hard_shiftTransferCancellationCard_onlyCurrentManager() {
		Employee shiftManager = buildEmployee("100000031", true, 10);
		Employee otherEmployee = buildEmployee("100000032", true, 10);
		Shift shift = new Shift(LocalDate.now(), ShiftType.MORNING, shiftManager, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> shift.transferCancellationCard(otherEmployee));

		shift.transferCancellationCard(shiftManager);
		assertTrue("Cancellation card should be transferred by shift manager", shift.isCancellationCardTransferred());
		assertEquals(shiftManager.getId(), shift.getCancellationCardTransferredBy().getId(), "Transfer actor should be the shift manager");
	}

	@Test
	public void easy_authenticationLogin_success() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		User user = new User("100000041", "secret");

		try {
			auth.registerUser(user);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue("Login should succeed for valid credentials", auth.login("100000041", "secret").isPresent());
		assertTrue("Auth state should be logged-in after successful login", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogin_employeeSuccess() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee employee = buildEmployee("100000042", false, 10);

		try {
			auth.registerUser(employee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue("Employee login should succeed with valid credentials", auth.login("100000042", "pass").isPresent());
		assertTrue("Logged-in user should be an Employee", auth.getCurrentUser().orElse(null) instanceof Employee);
	}

	@Test
	public void easy_authenticationLogin_hrManagerSuccess() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		HR_Manager hr = new HR_Manager("100000043", "hrpass");

		try {
			auth.registerUser(hr);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue("HR Manager login should succeed with valid credentials", auth.login("100000043", "hrpass").isPresent());
		assertTrue("Logged-in user should be an HR_Manager", auth.getCurrentUser().orElse(null) instanceof HR_Manager);
	}

	@Test
	public void easy_authenticationLogin_wrongPassword() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee employee = buildEmployee("100000044", false, 10);

		try {
			auth.registerUser(employee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertFalse("Login should fail with wrong password", auth.login("100000044", "wrongpass").isPresent());
		assertFalse("Auth state should remain logged-out after failed login", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogin_wrongId() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee employee = buildEmployee("100000045", false, 10);

		try {
			auth.registerUser(employee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertFalse("Login should fail with wrong ID", auth.login("100000046", "pass").isPresent());
		assertFalse("Auth state should remain logged-out after failed login", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogin_blocksFiredEmployee() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee firedEmployee = buildEmployee("100000051", false, 10);
		firedEmployee.setFired(true);

		try {
			auth.registerUser(firedEmployee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertFalse("Fired employee should be blocked from login", auth.login("100000051", "pass").isPresent());
		assertTrue("Fired credentials should be detected", auth.isFiredCredentials("100000051", "pass"));
	}

	@Test
	public void easy_authenticationLogout_employeeSuccess() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee employee = buildEmployee("100000052", false, 10);

		try {
			auth.registerUser(employee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		auth.login("100000052", "pass");
		assertTrue("Employee should be logged in before logout", auth.isLoggedIn());

		assertTrue("Logout should return true for a logged-in employee", auth.logout());
		assertFalse("Employee should be logged out after logout", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogout_hrManagerSuccess() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		HR_Manager hr = new HR_Manager("100000053", "hrpass");

		try {
			auth.registerUser(hr);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		auth.login("100000053", "hrpass");
		assertTrue("HR Manager should be logged in before logout", auth.isLoggedIn());

		assertTrue("Logout should return true for a logged-in HR Manager", auth.logout());
		assertFalse("HR Manager should be logged out after logout", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogout_whenNotLoggedIn() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());

		assertFalse("Logout should return false when no user is logged in", auth.logout());
		assertFalse("Auth state should remain logged-out", auth.isLoggedIn());
	}

	@Test
	public void easy_firedEmployee_recordStillRetrievable() {
		InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();
		Employee employee = buildEmployee("100000147", false, 10);

		try {
			repository.save(employee);
			employee.setFired(true);
			repository.save(employee);

			Employee found = repository.findById("100000147").orElse(null);
			assertFalse("Fired employee record should still exist in the repository", found == null);
			assertTrue("Retrieved employee should still be marked as fired", found.isFired());
			assertEquals(employee.getName(), found.getName(), "Fired employee's name should still be intact");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void easy_employeeRepositorySaveFindDelete_cycle() {
		InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();
		Employee employee = buildEmployee("100000061", false, 10);

		try {
			repository.save(employee);
			assertTrue("Saved employee should be retrievable by id", repository.findById("100000061").isPresent());
			repository.deleteById("100000061");
			assertFalse("Deleted employee should no longer exist", repository.findById("100000061").isPresent());
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void easy_addEmployee_persistsInRepository() {
		InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();
		Employee employee = buildEmployee("100000062", false, 10);

		try {
			repository.save(employee);
			Employee found = repository.findById("100000062").orElse(null);
			assertEquals(employee, found, "Added employee should be retrievable by id");
			assertEquals(1, repository.findAll().size(), "Repository should contain the added employee");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_userController_nonHrUser_cannotAddUpdateOrFireEmployee() {
		AuthenticationService authenticationService = new AuthenticationService(new InMemoryUserRepository());
		InMemoryEmployeeRepository employeeRepository = new InMemoryEmployeeRepository();
		UserController controller = new UserController(authenticationService, employeeRepository);
		User nonHr = new User("100000181", "pass");
		Employee employee = buildEmployee("100000182", false, 10);

		boolean addRejected = false;
		try {
			controller.addEmployee(nonHr, employee);
		} catch (IllegalArgumentException e) {
			addRejected = true;
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
		assertTrue("Non-HR user should not be allowed to add employee", addRejected);

		try {
			employeeRepository.save(employee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		boolean updateRejected = false;
		try {
			controller.updateEmployeeDetails(nonHr, employee, "Updated", 6000.0, 60.0, true);
		} catch (IllegalArgumentException e) {
			updateRejected = true;
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
		assertTrue("Non-HR user should not be allowed to update employee", updateRejected);

		boolean fireRejected = false;
		try {
			controller.fireEmployee(nonHr, employee.getId());
		} catch (IllegalArgumentException e) {
			fireRejected = true;
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
		assertTrue("Non-HR user should not be allowed to fire employee", fireRejected);
	}

	@Test
	public void easy_fireEmployee_marksAsFired() {
		Employee employee = buildEmployee("100000063", false, 10);

		assertFalse("Employee should not be fired initially", employee.isFired());

		employee.setFired(true);

		assertTrue("Employee should be marked as fired after setFired(true)", employee.isFired());
	}

	@Test
	public void easy_updateEmployee_name() {
		Employee employee = buildEmployee("100000064", false, 10);

		employee.setName("Updated Name");

		assertEquals("Updated Name", employee.getName(), "Employee name should reflect the update");
	}

	@Test
	public void easy_updateEmployee_salary() {
		Employee employee = buildEmployee("100000065", false, 10);
		Salary newSalary = new Salary(8000, 60, 0, EmploymentScope.FULL_TIME);

		employee.setSalary(newSalary);

		assertEquals(8000.0, employee.getSalary().getFinalSalary(), 0.0001, "Employee salary should reflect the update");
	}

	@Test
	public void easy_updateEmployee_bankAccount() {
		Employee employee = buildEmployee("100000066", false, 10);
		BankAccount newAccount = new BankAccount("20", "456", "111222");

		employee.setBankAccount(newAccount);

		assertEquals(newAccount, employee.getBankAccount(), "Employee bank account should reflect the update");
	}

	@Test
	public void easy_employeeDetails_startDate() {
		LocalDate startDate = LocalDate.of(2026, 4, 21);
		Employee employee = buildEmployee("100000067", false, 10);

		assertEquals(startDate, employee.getStartDate(), "Employee start date should match the value set at construction");
	}

	@Test
	public void easy_employeeDetails_employmentScope() {
		Employee employee = buildEmployee("100000068", false, 10);

		assertEquals(EmploymentScope.FULL_TIME, employee.getEmploymentTerms().getEmploymentScope(), "Employee employment scope should be FULL_TIME as set at construction");
	}

	@Test
	public void easy_employeeDetails_globalSalary() {
		Employee employee = buildEmployee("100000069", false, 10);

		assertEquals(5000.0, employee.getEmploymentTerms().getGlobalSalary(), 0.0001, "Employee global salary should match the value set at construction");
	}

	@Test
	public void easy_employeeDetails_hourlySalary() {
		Employee employee = buildEmployee("100000070", false, 10);

		assertEquals(50.0, employee.getEmploymentTerms().getHourlySalary(), 0.0001, "Employee hourly salary should match the value set at construction");
	}

	@Test
	public void easy_submissionDeadlineRepository_roundTrip() {
		InMemorySubmissionDeadlineRepository repository = new InMemorySubmissionDeadlineRepository();
		LocalDate deadline = LocalDate.of(2026, 4, 25);

		try {
			repository.save(deadline);
			assertEquals(deadline, repository.findCurrent().orElse(null), "Saved deadline should be loaded back");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_consoleReadLocalDate_acceptsDdMmYyyy() throws Exception {
		ConsolePresentation console = new ConsolePresentation();
		Scanner scanner = new Scanner("20-04-2026\n");

		java.lang.reflect.Method readLocalDate = ConsolePresentation.class
			.getDeclaredMethod("readLocalDate", Scanner.class, String.class);
		readLocalDate.setAccessible(true);

		LocalDate parsed = (LocalDate) readLocalDate.invoke(
			console,
			scanner,
			"Deadline for submitting constraints and preferences for the upcoming week (DD-MM-YYYY): "
		);

		assertEquals(LocalDate.of(2026, 4, 20), parsed, "Date should be parsed from DD-MM-YYYY input");
	}

	@Test
	public void easy_deadlinePolicy_validDeadlineAccepted() {
		SubmissionDeadlinePolicy policy = new SubmissionDeadlinePolicy();
		LocalDate today = LocalDate.of(2026, 4, 20); // Monday
		LocalDate validDeadline = LocalDate.of(2026, 4, 22); // Wednesday — within the week

		policy.validateManagerDeadline(validDeadline, today); // should not throw
	}

	@Test
	public void easy_deadlinePolicy_saturdayBoundaryAccepted() {
		SubmissionDeadlinePolicy policy = new SubmissionDeadlinePolicy();
		LocalDate today = LocalDate.of(2026, 4, 20); // Monday
		LocalDate saturday = LocalDate.of(2026, 4, 25); // Saturday of same week

		policy.validateManagerDeadline(saturday, today); // should not throw
	}

	@Test
	public void easy_deadlinePolicy_deadlineAfterSaturdayRejected() {
		SubmissionDeadlinePolicy policy = new SubmissionDeadlinePolicy();
		LocalDate today = LocalDate.of(2026, 4, 20); // Monday
		LocalDate nextWeek = LocalDate.of(2026, 4, 26); // Sunday — next week

		assertThrows(IllegalArgumentException.class, () -> policy.validateManagerDeadline(nextWeek, today));
	}

	@Test
	public void easy_deadlinePolicy_nullDeadlineRejected() {
		SubmissionDeadlinePolicy policy = new SubmissionDeadlinePolicy();
		LocalDate today = LocalDate.of(2026, 4, 20);

		assertThrows(IllegalArgumentException.class, () -> policy.validateManagerDeadline(null, today));
	}

	@Test
	public void hard_shiftController_maintainsShiftHistorySeparately() {
		ShiftController controller = new ShiftController();
		Shift activeShift = new Shift(
			LocalDate.of(2026, 4, 21),
			ShiftType.MORNING,
			buildEmployee("100000148", true, 10),
			1,
			1
		);
		Shift historicalShift = new Shift(
			LocalDate.of(2026, 4, 14),
			ShiftType.EVENING,
			buildEmployee("100000149", true, 10),
			1,
			1
		);

		controller.addShift(activeShift);
		controller.addToShiftHistory(historicalShift);

		assertTrue("Active shifts should contain the active shift", controller.getShifts().contains(activeShift));
		assertTrue("Shift history should contain the historical shift", controller.getShiftHistory().contains(historicalShift));
		assertFalse("Active shifts should not contain the historical shift", controller.getShifts().contains(historicalShift));
	}

	@Test
	public void hard_shiftController_assignNoConflict_autoApproved() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000081", "p");
		Employee employee = buildEmployee("100000101", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 21), ShiftType.MORNING, buildEmployee("100000111", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertEquals(1, shift.getAssignments().size(), "Shift should have one assignment");
		assertTrue("No-conflict assignment should be auto-approved", shift.getAssignments().get(0).isApproved());
	}

	@Test
	public void hard_shiftController_morningShift_assignmentStoredWithRole() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000154", "p");
		Employee employee = buildEmployee("100000155", false, 10);
		Shift morningShift = new Shift(LocalDate.of(2026, 5, 1), ShiftType.MORNING, buildEmployee("100000156", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, morningShift, Role.CASHIER);

		assertEquals(1, morningShift.getAssignments().size(), "Morning shift should contain exactly one assignment");
		assertEquals(Role.CASHIER, morningShift.getAssignments().get(0).getRole(), "Assignment role should be CASHIER for morning shift");
	}

	@Test
	public void hard_shiftController_eveningShift_assignmentStoredWithRole() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000157", "p");
		Employee employee = buildEmployeeWithRoles("100000158", false, 10, Collections.singleton(Role.STOREKEEPER));
		Shift eveningShift = new Shift(LocalDate.of(2026, 5, 2), ShiftType.EVENING, buildEmployee("100000159", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, eveningShift, Role.STOREKEEPER);

		assertEquals(1, eveningShift.getAssignments().size(), "Evening shift should contain exactly one assignment");
		assertEquals(Role.STOREKEEPER, eveningShift.getAssignments().get(0).getRole(), "Assignment role should be STOREKEEPER for evening shift");
	}

	@Test
	public void hard_shiftController_assignConstraintConflict_pending() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000082", "p");
		Employee employee = buildEmployee("100000102", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 22), ShiftType.MORNING, buildEmployee("100000112", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addConstraint(
			new Constraint(shift.getDate().getDayOfWeek(), ShiftType.MORNING)
		);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		ShiftAssignment assignment = shift.getAssignments().get(0);
		assertTrue("Conflict assignment should stay pending employee response", assignment.isPending());
		assertFalse("Pending assignment should not be approved yet", assignment.isApproved());
	}

	@Test
	public void hard_shiftController_assignVacationDay_blockedForManager() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000091", "p");
		Employee employee = buildEmployee("100000121", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 22), ShiftType.MORNING, buildEmployee("100000122", true, 10), 1, 1);

		DayOfWeek vacationDay = shift.getDate().getDayOfWeek();
		employee.getWeeklyAvailabilityRequest().addConstraint(new Constraint(vacationDay, ShiftType.MORNING));
		employee.getWeeklyAvailabilityRequest().addConstraint(new Constraint(vacationDay, ShiftType.MORNING_OVERTIME));
		employee.getWeeklyAvailabilityRequest().addConstraint(new Constraint(vacationDay, ShiftType.EVENING));
		employee.getWeeklyAvailabilityRequest().addConstraint(new Constraint(vacationDay, ShiftType.DOUBLE_SHIFT));

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));
	}

	@Test
	public void hard_shiftController_respondReject_removesAssignment() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000083", "p");
		Employee employee = buildEmployee("100000103", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 23), ShiftType.MORNING, buildEmployee("100000113", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addConstraint(
			new Constraint(shift.getDate().getDayOfWeek(), ShiftType.MORNING)
		);
		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		ShiftAssignment pending = shift.getAssignments().get(0);
		controller.respondToAssignment(employee, pending, false);

		assertEquals(0, shift.getAssignments().size(), "Rejected pending assignment should be removed from shift");
	}

	@Test
	public void hard_shiftController_respondAccept_approvesAssignment() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000093", "p");
		Employee employee = buildEmployee("100000110", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 23), ShiftType.MORNING, buildEmployee("100000125", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addConstraint(
			new Constraint(shift.getDate().getDayOfWeek(), ShiftType.MORNING)
		);
		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		ShiftAssignment pending = shift.getAssignments().get(0);
		assertTrue("Assignment should be pending before employee responds", pending.isPending());

		controller.respondToAssignment(employee, pending, true);

		assertFalse("Assignment should no longer be pending after employee accepts", pending.isPending());
		assertTrue("Assignment should be approved after employee accepts", pending.isApproved());
		assertEquals(1, shift.getAssignments().size(), "Assignment should remain in the shift after acceptance");
	}

	@Test
	public void hard_shiftController_doubleShiftRequiresPreference() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000084", "p");
		Employee employee = buildEmployee("100000104", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 24), ShiftType.DOUBLE_SHIFT, buildEmployee("100000114", true, 10), 1, 1);

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));

		employee.getWeeklyAvailabilityRequest().addPreference(
			new Preference(shift.getDate().getDayOfWeek(), ShiftType.DOUBLE_SHIFT)
		);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);
		assertEquals(1, shift.getAssignments().size(), "Assignment should succeed after matching preference is provided");
	}

	@Test
	public void hard_shiftController_overtimeShiftBlockedWithoutPreference() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000088", "p");
		Employee employee = buildEmployee("100000105", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 24), ShiftType.MORNING_OVERTIME, buildEmployee("100000119", true, 10), 1, 1);

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));
	}

	@Test
	public void hard_shiftController_overtimeShiftRequiresPreference() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000089", "p");
		Employee employee = buildEmployee("100000106", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 24), ShiftType.MORNING_OVERTIME, buildEmployee("100000120", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addPreference(
			new Preference(shift.getDate().getDayOfWeek(), ShiftType.MORNING_OVERTIME)
		);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertEquals(1, shift.getAssignments().size(), "Overtime assignment should succeed after matching preference is provided");
	}

	@Test
	public void easy_employeeRoles_storedAndRetrievable() {
		Employee employee = buildEmployeeWithRoles("100000142", false, 10, Collections.singleton(Role.STOREKEEPER));

		assertTrue("Employee's roles should contain the role assigned at construction", employee.getRoles().contains(Role.STOREKEEPER));
		assertFalse("Employee's roles should not contain a role that was not assigned", employee.getRoles().contains(Role.CASHIER));
	}

	@Test
	public void hard_shiftController_assignRoleMismatch_rejected() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000097", "p");
		Employee employee = buildEmployeeWithRoles("100000143", false, 10, Collections.singleton(Role.STOREKEEPER));
		Shift shift = new Shift(LocalDate.of(2026, 4, 21), ShiftType.MORNING, buildEmployee("100000144", true, 10), 1, 1);

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));
	}

	@Test
	public void hard_shiftController_assignSameEmployeeTwice_rejected() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000098", "p");
		Employee employee = buildEmployee("100000145", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 21), ShiftType.MORNING, buildEmployee("100000146", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));
	}

	@Test
	public void hard_shiftController_substituteRoleMismatch_rejected() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000085", "p");
		Employee original = buildEmployee("100000131", false, 10);
		Employee replacement = buildEmployeeWithRoles("100000132", false, 10, Collections.singleton(Role.STOREKEEPER));
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("100000115", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.substituteEmployee(hr, shift, original, replacement));
	}

	@Test
	public void hard_shiftController_substitute_success() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000094", "p");
		Employee original = buildEmployee("100000135", false, 10);
		Employee replacement = buildEmployee("100000136", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("100000126", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		controller.substituteEmployee(hr, shift, original, replacement);

		assertEquals(1, shift.getAssignments().size(), "Shift should still have one assignment after substitution");
		assertEquals("100000136", shift.getAssignments().get(0).getEmployee().getId(), "Replacement should be the assigned employee after substitution");
	}

	@Test
	public void hard_shiftController_substitute_rejectsAlreadyAssignedReplacement() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000095", "p");
		Employee original = buildEmployee("100000137", false, 10);
		Employee alreadyAssigned = buildEmployee("100000138", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("100000127", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		controller.assignEmployeeToShift(hr, alreadyAssigned, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.substituteEmployee(hr, shift, original, alreadyAssigned));
	}

	@Test
	public void hard_shiftController_substitute_rejectsNonHrUser() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000096", "p");
		Employee original = buildEmployee("100000139", false, 10);
		Employee replacement = buildEmployee("100000140", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("100000128", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.substituteEmployee(new employee.domain.User("100000023", "p"), shift, original, replacement));
	}

	@Test
	public void hard_shiftController_cancellationRequestAndHandleWithSubstitution() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000086", "p");
		Employee original = buildEmployee("100000133", false, 10);
		Employee replacement = buildEmployee("100000134", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 27), ShiftType.EVENING, buildEmployee("100000116", true, 10), 1, 1);

		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		controller.requestShiftCancellation(original, shift);

		ShiftAssignment request = controller.getCancellationRequests().get(0);
		controller.handleCancellationWithSubstitution(hr, request, replacement);

		assertEquals(1, shift.getAssignments().size(), "After handling cancellation, shift should still have one assignment");
			assertEquals("100000134", shift.getAssignments().get(0).getEmployee().getId(), "Replacement employee should be assigned");
	}

	@Test
	public void hard_shiftController_cancellationRequest_appearsInList() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000090", "p");
		Employee employee = buildEmployee("100000107", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 28), ShiftType.MORNING, buildEmployee("100000123", true, 10), 1, 1);

		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertTrue("No cancellation requests expected before submission", controller.getCancellationRequests().isEmpty());

		controller.requestShiftCancellation(employee, shift);

		assertEquals(1, controller.getCancellationRequests().size(), "Cancellation request should appear in the list after submission");
		assertEquals(employee.getId(), controller.getCancellationRequests().get(0).getEmployee().getId(), "Cancellation request should belong to the requesting employee");
	}

	@Test
	public void hard_shiftController_cancellationRequest_rejectedForNonAssignedEmployee() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000092", "p");
		Employee assigned = buildEmployee("100000108", false, 10);
		Employee notAssigned = buildEmployee("100000109", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 29), ShiftType.MORNING, buildEmployee("100000124", true, 10), 1, 1);

		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, assigned, shift, Role.CASHIER);

		assertThrows(IllegalArgumentException.class, () -> controller.requestShiftCancellation(notAssigned, shift));
	}

	@Test
	public void hard_shiftController_shiftTypeHoursMapping_morningMorningOvertimeEvening() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000099", "p");
		Employee employee = buildEmployee("100000150", false, 10);

		Shift morning = new Shift(LocalDate.of(2026, 4, 28), ShiftType.MORNING, buildEmployee("100000151", true, 10), 1, 1);
		Shift morningOvertime = new Shift(LocalDate.of(2026, 4, 29), ShiftType.MORNING_OVERTIME, buildEmployee("100000152", true, 10), 1, 1);
		Shift evening = new Shift(LocalDate.of(2026, 4, 30), ShiftType.EVENING, buildEmployee("100000153", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addPreference(
			new Preference(morningOvertime.getDate().getDayOfWeek(), ShiftType.MORNING_OVERTIME)
		);

		controller.addShift(morning);
		controller.addShift(morningOvertime);
		controller.addShift(evening);

		controller.assignEmployeeToShift(hr, employee, morning, Role.CASHIER);
		controller.assignEmployeeToShift(hr, employee, morningOvertime, Role.CASHIER);
		controller.assignEmployeeToShift(hr, employee, evening, Role.CASHIER);

		double workedHours = controller.calculateWorkedHoursForEmployee(employee);

		assertEquals(26.0, workedHours, 0.0001, "MORNING (8h) + MORNING_OVERTIME (10h) + EVENING (8h) should total 26 hours");
	}

	@Test
	public void hard_shiftController_recalculateSalaryFromApprovedAssignments() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000087", "p");
		Employee employee = buildEmployee("100000141", false, 10);
		Shift morning = new Shift(LocalDate.of(2026, 4, 28), ShiftType.MORNING, buildEmployee("100000117", true, 10), 1, 1);
		Shift evening = new Shift(LocalDate.of(2026, 4, 29), ShiftType.EVENING, buildEmployee("100000118", true, 10), 1, 1);

		controller.addShift(morning);
		controller.addShift(evening);
		controller.assignEmployeeToShift(hr, employee, morning, Role.CASHIER);
		controller.assignEmployeeToShift(hr, employee, evening, Role.CASHIER);

		double workedHours = controller.calculateWorkedHoursForEmployee(employee);
		double finalSalary = controller.recalculateEmployeeSalary(employee);

		assertEquals(16.0, workedHours, 0.0001, "Two approved regular shifts should total 16 worked hours");
		assertEquals(5000.0, finalSalary, 0.0001, "16h is below full-time overtime threshold so salary stays base");
	}

	private static Employee buildEmployee(String id, boolean canManageShift, int vacationDays) {
		return buildEmployee(id, canManageShift, vacationDays, DEFAULT_TEST_BRANCH);
	}

	private static Employee buildEmployee(String id, boolean canManageShift, int vacationDays, Branch branch) {
		Salary salary = new Salary(5000, 50, 0, EmploymentScope.FULL_TIME);
		EmploymentTerms terms = new EmploymentTerms(
			LocalDate.of(2026, 4, 21),
			EmploymentScope.FULL_TIME,
			salary.getGlobalSalary(),
			salary.getHourlySalary(),
			vacationDays
		);

		Set<Role> roles = Collections.singleton(Role.CASHIER);

		return new Employee(
			id,
			"pass",
			new BankAccount("10", "123", "000111"),
			"Emp " + id,
			salary,
			EmploymentType.REGULAR,
			terms,
			roles,
			canManageShift,
			false,
			null,
			new WeeklyAvailabilityRequest(),
			branch
		);
	}

	private static Employee buildEmployeeWithRoles(String id, boolean canManageShift, int vacationDays, Set<Role> roles) {
		return buildEmployeeWithRoles(id, canManageShift, vacationDays, roles, DEFAULT_TEST_BRANCH);
	}

	private static Employee buildEmployeeWithRoles(String id, boolean canManageShift, int vacationDays, Set<Role> roles, Branch branch) {
		Salary salary = new Salary(5000, 50, 0, EmploymentScope.FULL_TIME);
		EmploymentTerms terms = new EmploymentTerms(
			LocalDate.of(2026, 4, 21),
			EmploymentScope.FULL_TIME,
			salary.getGlobalSalary(),
			salary.getHourlySalary(),
			vacationDays
		);

		return new Employee(
			id,
			"pass",
			new BankAccount("10", "123", "000111"),
			"Emp " + id,
			salary,
			EmploymentType.REGULAR,
			terms,
			roles,
			canManageShift,
			false,
			null,
			new WeeklyAvailabilityRequest(),
			branch
		);
	}

	private static Employee buildGlobalDriver(String id) {
		return new Employee(
			id,
			"pass",
			new BankAccount("10", "123", "000222"),
			"Global Driver " + id,
			new Salary(5000, 50, 0, EmploymentScope.FULL_TIME),
			EmploymentType.REGULAR,
			new EmploymentTerms(LocalDate.of(2026, 4, 21), EmploymentScope.FULL_TIME, 5000, 50, 10),
			Collections.singleton(Role.DRIVER),
			false,
			false,
			null,
			new WeeklyAvailabilityRequest(),
			null
		);
	}

	@Test
	public void branch_creation_successWithValidDetails() {
		Branch branch = new Branch("B-001", "Main Branch", "Downtown");

		assertEquals("B-001", branch.getBranchId(), "Branch ID should match");
		assertEquals("Main Branch", branch.getBranchName(), "Branch name should match");
		assertEquals("Downtown", branch.getLocation(), "Branch location should match");
	}

	@Test
	public void branch_creationWithSite_successWithBranchSiteType() {
		ShippingZone zone = new ShippingZone("Z1", "Zone 1");
		Site branchSite = new Site(
			"Branch Stop",
			"123 Main St",
			"555-0001",
			"John Doe",
			zone,
			SiteType.BRANCH,
			null
		);
		Branch branch = new Branch("B-002", "Secondary Branch", "Uptown", branchSite);

		assertEquals(branchSite, branch.getDeliveryStop(), "Branch should have delivery stop");
		assertEquals(SiteType.BRANCH, branchSite.getSiteType(), "Delivery stop should be BRANCH type");
	}

	@Test
	public void branch_rejection_whenSiteIsNotBranchType() {
		ShippingZone zone = new ShippingZone("Z2", "Zone 2");
		Site regularSite = new Site(
			"Regular Stop",
			"456 Oak Ave",
			"555-0002",
			"Jane Smith",
			zone,
			SiteType.REGULAR,
			null
		);

		assertThrows(IllegalArgumentException.class, () ->
			new Branch("B-003", "Bad Branch", "Midtown", regularSite)
		);
	}

	@Test
	public void employee_branchSpecific_successWithCashierAndBranch() {
		Branch branch = new Branch("B-004", "Test Branch", "Test Location");
		Employee branchEmployee = new Employee(
			"100000401",
			"pass",
			new BankAccount("10", "123", "001"),
			"Branch Emp One",
			new Salary(5000, 50, 0, EmploymentScope.FULL_TIME),
			EmploymentType.REGULAR,
			new EmploymentTerms(LocalDate.now(), EmploymentScope.FULL_TIME, 5000, 50, 10),
			Collections.singleton(Role.CASHIER),
			false,
			false,
			null,
			new WeeklyAvailabilityRequest(),
			branch
		);

		assertEquals(branch, branchEmployee.getBranch(), "Employee should have branch assigned");
	}

	@Test
	public void employee_branchSpecific_rejectionWithCashierButNoBranch() {
		assertThrows(IllegalArgumentException.class, () ->
			new Employee(
				"100000402",
				"pass",
				new BankAccount("10", "123", "002"),
				"Bad Branch Emp",
				new Salary(5000, 50, 0, EmploymentScope.FULL_TIME),
				EmploymentType.REGULAR,
				new EmploymentTerms(LocalDate.now(), EmploymentScope.FULL_TIME, 5000, 50, 10),
				Collections.singleton(Role.STOREKEEPER),
				false,
				false,
				null,
				new WeeklyAvailabilityRequest(),
				null
			)
		);
	}

	@Test
	public void employee_globalDriver_creationSuccess() {
		Employee globalDriver = buildGlobalDriver("100000403");

		assertEquals(null, globalDriver.getBranch(), "Global driver should have no branch");
		assertTrue("Global driver should be authorized for DRIVER role",
			globalDriver.getAuthorizedRoles().contains(Role.DRIVER));
	}

	@Test
	public void employee_globalDriver_rejectionWhenAssignedBranch() {
		Branch branch = new Branch("B-005", "Driver Test Branch", "Test");

		assertThrows(IllegalArgumentException.class, () ->
			new Employee(
				"100000404",
				"pass",
				new BankAccount("10", "123", "004"),
				"Bad Global Driver",
				new Salary(5000, 50, 0, EmploymentScope.FULL_TIME),
				EmploymentType.REGULAR,
				new EmploymentTerms(LocalDate.now(), EmploymentScope.FULL_TIME, 5000, 50, 10),
				Collections.singleton(Role.DRIVER),
				false,
				false,
				null,
				new WeeklyAvailabilityRequest(),
				branch
			)
		);
	}

	@Test
	public void employee_mixedRoles_driverAndCashierRequiresBranch() {
		Branch branch = new Branch("B-006", "Mixed Role Branch", "Test");
		Employee mixedEmployee = new Employee(
			"100000405",
			"pass",
			new BankAccount("10", "123", "005"),
			"Mixed Role Employee",
			new Salary(5000, 50, 0, EmploymentScope.FULL_TIME),
			EmploymentType.REGULAR,
			new EmploymentTerms(LocalDate.now(), EmploymentScope.FULL_TIME, 5000, 50, 10),
			new HashSet<>(Arrays.asList(Role.DRIVER, Role.CASHIER)),
			false,
			false,
			null,
			new WeeklyAvailabilityRequest(),
			branch
		);

		assertEquals(branch, mixedEmployee.getBranch(), "Mixed role employee should have branch");
	}

	@Test
	public void shift_branchAssignment_successWhenCreatedWithBranch() {
		Branch branch = new Branch("B-010", "Shift Test Branch", "Test");
		Employee manager = buildEmployee("100000410", true, 10, branch);

		Shift shift = new Shift(
			LocalDate.of(2026, 5, 15),
			ShiftType.MORNING,
			manager,
			1,
			1,
			branch
		);

		assertEquals(branch, shift.getBranch(), "Shift should have branch assigned");
	}

	@Test
	public void shiftAssignment_successWhenEmployeeAndShiftSameBranch() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000411", "pass");
		Branch branch = new Branch("B-011", "Assignment Test Branch", "Test");

		Employee manager = buildEmployee("100000412", true, 10, branch);
		Employee employee = buildEmployee("100000413", false, 10, branch);

		Shift shift = new Shift(
			LocalDate.of(2026, 5, 16),
			ShiftType.MORNING,
			manager,
			1,
			1,
			branch
		);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertEquals(1, shift.getAssignments().size(), "Employee should be assigned to shift");
		assertEquals(employee.getId(), shift.getAssignments().get(0).getEmployee().getId(), "Assigned employee should match");
	}

	@Test
	public void shiftAssignment_rejectionWhenEmployeeAndShiftDifferentBranches() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000414", "pass");
		Branch branch1 = new Branch("B-012", "Branch One", "Location One");
		Branch branch2 = new Branch("B-013", "Branch Two", "Location Two");

		Employee manager = buildEmployee("100000415", true, 10, branch1);
		Employee employee = buildEmployee("100000416", false, 10, branch2);

		Shift shift = new Shift(
			LocalDate.of(2026, 5, 17),
			ShiftType.MORNING,
			manager,
			1,
			1,
			branch1
		);

		assertThrows(IllegalArgumentException.class, () ->
			controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER)
		);
	}

	@Test
	public void shiftAssignment_globalDriverSuccessInAnyShinftRegardlessOfBranch() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000417", "pass");
		Branch branch = new Branch("B-014", "Driver Assignment Branch", "Test");

		Employee manager = buildEmployee("100000418", true, 10, branch);
		Employee globalDriver = buildGlobalDriver("100000419");

		Shift shift = new Shift(
			LocalDate.of(2026, 5, 18),
			ShiftType.EVENING,
			manager,
			0,
			0,
			branch
		);

		controller.assignEmployeeToShift(hr, globalDriver, shift, Role.DRIVER);

		assertEquals(1, shift.getAssignments().size(), "Global driver should be assigned to shift");
		assertEquals(globalDriver.getId(), shift.getAssignments().get(0).getEmployee().getId(), "Assigned driver should match");
	}

	@Test
	public void substitution_globalDriverAcceptedInAnyBranchShift() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000420", "pass");
		Branch branch = new Branch("B-015", "Substitution Test Branch", "Test");

		Employee manager = buildEmployee("100000421", true, 10, branch);
		Employee original = buildEmployee("100000422", false, 10, branch);
		Employee globalDriver = buildGlobalDriver("100000423");

		Shift shift = new Shift(
			LocalDate.of(2026, 5, 19),
			ShiftType.MORNING,
			manager,
			1,
			0,
			branch
		);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		controller.substituteEmployee(hr, shift, original, globalDriver);

		assertEquals(1, shift.getAssignments().size(), "Shift should still have one assignment after substitution");
		assertEquals(globalDriver.getId(), shift.getAssignments().get(0).getEmployee().getId(), "Global driver should replace original employee");
	}

	@Test
	public void substitution_globalDriverSuccessAcrossMultipleBranches() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("100000424", "pass");
		Branch branch1 = new Branch("B-016", "First Branch", "Loc1");
		Branch branch2 = new Branch("B-017", "Second Branch", "Loc2");

		Employee manager1 = buildEmployee("100000425", true, 10, branch1);
		Employee original1 = buildEmployee("100000426", false, 10, branch1);

		Employee manager2 = buildEmployee("100000427", true, 10, branch2);
		Employee original2 = buildEmployee("100000428", false, 10, branch2);

		Employee globalDriver = buildGlobalDriver("100000429");

		Shift shift1 = new Shift(LocalDate.of(2026, 5, 20), ShiftType.MORNING, manager1, 1, 0, branch1);
		Shift shift2 = new Shift(LocalDate.of(2026, 5, 21), ShiftType.EVENING, manager2, 1, 0, branch2);

		controller.assignEmployeeToShift(hr, original1, shift1, Role.CASHIER);
		controller.assignEmployeeToShift(hr, original2, shift2, Role.CASHIER);

		controller.substituteEmployee(hr, shift1, original1, globalDriver);
		controller.substituteEmployee(hr, shift2, original2, globalDriver);

		assertEquals(globalDriver.getId(), shift1.getAssignments().get(0).getEmployee().getId(), "Global driver should be in shift1");
		assertEquals(globalDriver.getId(), shift2.getAssignments().get(0).getEmployee().getId(), "Global driver should be in shift2");
	}

	private static void assertThrows(Class<? extends Throwable> expected, Runnable action) {
		try {
			action.run();
		} catch (Throwable actual) {
			if (expected.isInstance(actual)) {
				return;
			}
			throw new AssertionError(
				"Expected " + expected.getSimpleName() + " but got " + actual.getClass().getSimpleName()
			);
		}
		throw new AssertionError("Expected exception " + expected.getSimpleName() + " but nothing was thrown");
	}
}