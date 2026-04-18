package employees.tests;

import employees.domain.BankAccount;
import employees.domain.Constraint;
import employees.domain.Employee;
import employees.domain.EmploymentScope;
import employees.domain.EmploymentTerms;
import employees.domain.EmploymentType;
import employees.domain.HR_Manager;
import employees.domain.Preference;
import employees.domain.Role;
import employees.domain.Salary;
import employees.domain.Shift;
import employees.domain.ShiftAssignment;
import employees.domain.ShiftType;
import employees.domain.User;
import employees.domain.WeeklyAvailabilityRequest;
import employees.presentation.ShiftController;
import employees.repository.RepositoryException;
import employees.repository.impl.InMemoryEmployeeRepository;
import employees.repository.impl.InMemorySubmissionDeadlineRepository;
import employees.repository.impl.InMemoryUserRepository;
import employees.service.AuthenticationService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Tests {
	private static int passed = 0;
	private static int failed = 0;

	public static void main(String[] args) {
		run("easy_salaryNoOvertime_whenWorkedBelowThreshold", Tests::easy_salaryNoOvertime_whenWorkedBelowThreshold);
		run("easy_salaryOvertime_forPartTimeEmployee", Tests::easy_salaryOvertime_forPartTimeEmployee);
		run("easy_salaryRejectsNullEmploymentScope", Tests::easy_salaryRejectsNullEmploymentScope);
		run("easy_weeklyAvailabilityReset_clearsData", Tests::easy_weeklyAvailabilityReset_clearsData);
		run("easy_shiftAssignmentPendingLogic", Tests::easy_shiftAssignmentPendingLogic);
		run("hard_employeeVacationConsumption_reducesBalance", Tests::hard_employeeVacationConsumption_reducesBalance);
		run("hard_employeeVacationConsumption_rejectsOveruse", Tests::hard_employeeVacationConsumption_rejectsOveruse);
		run("hard_employeeFixedDayOff_canOnlyBeSetOnce", Tests::hard_employeeFixedDayOff_canOnlyBeSetOnce);
		run("hard_shiftAssignManager_requiresHrManager", Tests::hard_shiftAssignManager_requiresHrManager);
		run("hard_shiftAssignManager_requiresCertifiedCandidate", Tests::hard_shiftAssignManager_requiresCertifiedCandidate);
		run("hard_shiftConfigureRoleCounts_requiresHrManager", Tests::hard_shiftConfigureRoleCounts_requiresHrManager);
		run("hard_shiftTransferCancellationCard_onlyCurrentManager", Tests::hard_shiftTransferCancellationCard_onlyCurrentManager);

		run("easy_authenticationLogin_success", Tests::easy_authenticationLogin_success);
		run("easy_authenticationLogin_blocksFiredEmployee", Tests::easy_authenticationLogin_blocksFiredEmployee);
		run("easy_employeeRepositorySaveFindDelete_cycle", Tests::easy_employeeRepositorySaveFindDelete_cycle);
		run("easy_submissionDeadlineRepository_roundTrip", Tests::easy_submissionDeadlineRepository_roundTrip);

		run("hard_shiftController_assignNoConflict_autoApproved", Tests::hard_shiftController_assignNoConflict_autoApproved);
		run("hard_shiftController_assignConstraintConflict_pending", Tests::hard_shiftController_assignConstraintConflict_pending);
		run("hard_shiftController_respondReject_removesAssignment", Tests::hard_shiftController_respondReject_removesAssignment);
		run("hard_shiftController_doubleShiftRequiresPreference", Tests::hard_shiftController_doubleShiftRequiresPreference);
		run("hard_shiftController_substituteRoleMismatch_rejected", Tests::hard_shiftController_substituteRoleMismatch_rejected);
		run("hard_shiftController_cancellationRequestAndHandleWithSubstitution", Tests::hard_shiftController_cancellationRequestAndHandleWithSubstitution);
		run("hard_shiftController_recalculateSalaryFromApprovedAssignments", Tests::hard_shiftController_recalculateSalaryFromApprovedAssignments);

		System.out.println("\n----------------------------------------------");
		System.out.println("Total tests: " + (passed + failed));
		System.out.println("Passed: " + passed);
		System.out.println("Failed: " + failed);
		System.out.println("----------------------------------------------");

		if (failed > 0) {
			throw new AssertionError("Some tests failed");
		}
	}

	private static void run(String name, Runnable test) {
		try {
			test.run();
			passed++;
			System.out.println("[PASS] " + name);
		} catch (Throwable t) {
			failed++;
			System.out.println("[FAIL] " + name + " -> " + t.getMessage());
		}
	}

	private static void easy_salaryNoOvertime_whenWorkedBelowThreshold() {
		Salary salary = new Salary(5000, 50, 120, EmploymentScope.FULL_TIME);

		assertEquals(0.0, salary.getOvertimeHours(), 0.0001, "Overtime should be zero for full-time under 190h");
		assertEquals(5000.0, salary.getFinalSalary(), 0.0001, "Final salary should equal base when no overtime");
	}

	private static void easy_salaryOvertime_forPartTimeEmployee() {
		Salary salary = new Salary(3000, 40, 100, EmploymentScope.PART_TIME);

		assertEquals(5.0, salary.getOvertimeHours(), 0.0001, "Part-time overtime should be workedHours - 95");
		assertEquals(3200.0, salary.getFinalSalary(), 0.0001, "Final salary should include overtime pay");
	}

	private static void easy_salaryRejectsNullEmploymentScope() {
		Salary salary = new Salary(3000, 30, 100);

		assertThrows(IllegalArgumentException.class, () -> salary.setEmploymentScope(null));
	}

	private static void easy_weeklyAvailabilityReset_clearsData() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();
		request.addConstraint(new employees.domain.Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		request.addPreference(new employees.domain.Preference(DayOfWeek.MONDAY, ShiftType.EVENING));

		LocalDate newWeek = LocalDate.of(2026, 4, 20);
		request.resetForWeek(newWeek);

		assertTrue(request.getConstraints().isEmpty(), "Constraints should be cleared after reset");
		assertTrue(request.getPreferences().isEmpty(), "Preferences should be cleared after reset");
		assertEquals(newWeek, request.getWeekStartDate(), "Week start date should be updated");
	}

	private static void easy_shiftAssignmentPendingLogic() {
		ShiftAssignment assignment = new ShiftAssignment(null, null, Role.CASHIER, true);

		assertTrue(assignment.isPending(), "Assignment requiring approval should start as pending");

		assignment.setApproved(true);
		assertTrue(!assignment.isPending(), "Approved assignment should no longer be pending");
	}

	private static void hard_employeeVacationConsumption_reducesBalance() {
		Employee employee = buildEmployee("e1", false, 10);

		employee.consumeVacationDays(3);

		assertEquals(7, employee.getVacationDaysBalance(), "Vacation balance should decrease by consumed amount");
	}

	private static void hard_employeeVacationConsumption_rejectsOveruse() {
		Employee employee = buildEmployee("e2", false, 2);

		assertThrows(IllegalArgumentException.class, () -> employee.consumeVacationDays(3));
	}

	private static void hard_employeeFixedDayOff_canOnlyBeSetOnce() {
		Employee employee = buildEmployee("e3", false, 10);

		employee.setFixedDayOff(DayOfWeek.SUNDAY);
		assertEquals(DayOfWeek.SUNDAY, employee.getFixedDayOff(), "Fixed day off should be set the first time");

		assertThrows(IllegalStateException.class, () -> employee.setFixedDayOff(DayOfWeek.MONDAY));
	}

	private static void hard_shiftAssignManager_requiresHrManager() {
		Employee candidate = buildEmployee("m1", true, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new employees.domain.User("u1", "p"), candidate));
	}

	private static void hard_shiftAssignManager_requiresCertifiedCandidate() {
		Employee notCertified = buildEmployee("m2", false, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new HR_Manager("hr1", "pass"), notCertified));
	}

	private static void hard_shiftConfigureRoleCounts_requiresHrManager() {
		Shift shift = new Shift();
		Map<Role, Integer> customCounts = new EnumMap<>(Role.class);
		customCounts.put(Role.CASHIER, 3);

		assertThrows(IllegalArgumentException.class, () -> shift.configureRequiredRoleCounts(new employees.domain.User("u2", "p"), customCounts));
	}

	private static void hard_shiftTransferCancellationCard_onlyCurrentManager() {
		Employee shiftManager = buildEmployee("manager", true, 10);
		Employee otherEmployee = buildEmployee("other", true, 10);
		Shift shift = new Shift(LocalDate.now(), ShiftType.MORNING, shiftManager, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> shift.transferCancellationCard(otherEmployee));

		shift.transferCancellationCard(shiftManager);
		assertTrue(shift.isCancellationCardTransferred(), "Cancellation card should be transferred by shift manager");
		assertEquals(shiftManager.getId(), shift.getCancellationCardTransferredBy().getId(), "Transfer actor should be the shift manager");
		assertTrue(shift.getCancellationCardTransferTime() != null, "Transfer time should be recorded");
	}

	private static void easy_authenticationLogin_success() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		User user = new User("u-login", "secret");

		try {
			auth.registerUser(user);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue(auth.login("u-login", "secret").isPresent(), "Login should succeed for valid credentials");
		assertTrue(auth.isLoggedIn(), "Auth state should be logged-in after successful login");
	}

	private static void easy_authenticationLogin_blocksFiredEmployee() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee firedEmployee = buildEmployee("fired-1", false, 10);
		firedEmployee.setFired(true);

		try {
			auth.registerUser(firedEmployee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue(!auth.login("fired-1", "pass").isPresent(), "Fired employee should be blocked from login");
		assertTrue(auth.isFiredCredentials("fired-1", "pass"), "Fired credentials should be detected");
	}

	private static void easy_employeeRepositorySaveFindDelete_cycle() {
		InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();
		Employee employee = buildEmployee("repo-1", false, 10);

		try {
			repository.save(employee);
			assertTrue(repository.findById("repo-1").isPresent(), "Saved employee should be retrievable by id");
			repository.deleteById("repo-1");
			assertTrue(!repository.findById("repo-1").isPresent(), "Deleted employee should no longer exist");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	private static void easy_submissionDeadlineRepository_roundTrip() {
		InMemorySubmissionDeadlineRepository repository = new InMemorySubmissionDeadlineRepository();
		LocalDate deadline = LocalDate.of(2026, 4, 25);

		try {
			repository.save(deadline);
			assertEquals(deadline, repository.findCurrent().orElse(null), "Saved deadline should be loaded back");
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	private static void hard_shiftController_assignNoConflict_autoApproved() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a1", "p");
		Employee employee = buildEmployee("assign-1", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 21), ShiftType.MORNING, buildEmployee("mgr-1", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertEquals(1, shift.getAssignments().size(), "Shift should have one assignment");
		assertTrue(shift.getAssignments().get(0).isApproved(), "No-conflict assignment should be auto-approved");
	}

	private static void hard_shiftController_assignConstraintConflict_pending() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a2", "p");
		Employee employee = buildEmployee("assign-2", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 22), ShiftType.MORNING, buildEmployee("mgr-2", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addConstraint(
			new Constraint(shift.getDate().getDayOfWeek(), ShiftType.MORNING)
		);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		ShiftAssignment assignment = shift.getAssignments().get(0);
		assertTrue(assignment.isPending(), "Conflict assignment should stay pending employee response");
		assertTrue(!assignment.isApproved(), "Pending assignment should not be approved yet");
	}

	private static void hard_shiftController_respondReject_removesAssignment() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a3", "p");
		Employee employee = buildEmployee("assign-3", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 23), ShiftType.MORNING, buildEmployee("mgr-3", true, 10), 1, 1);

		employee.getWeeklyAvailabilityRequest().addConstraint(
			new Constraint(shift.getDate().getDayOfWeek(), ShiftType.MORNING)
		);
		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		ShiftAssignment pending = shift.getAssignments().get(0);
		controller.respondToAssignment(employee, pending, false);

		assertEquals(0, shift.getAssignments().size(), "Rejected pending assignment should be removed from shift");
	}

	private static void hard_shiftController_doubleShiftRequiresPreference() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a4", "p");
		Employee employee = buildEmployee("assign-4", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 24), ShiftType.DOUBLE_SHIFT, buildEmployee("mgr-4", true, 10), 1, 1);

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));

		employee.getWeeklyAvailabilityRequest().addPreference(
			new Preference(shift.getDate().getDayOfWeek(), ShiftType.DOUBLE_SHIFT)
		);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);
		assertEquals(1, shift.getAssignments().size(), "Assignment should succeed after matching preference is provided");
	}

	private static void hard_shiftController_substituteRoleMismatch_rejected() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a5", "p");
		Employee original = buildEmployee("orig-1", false, 10);
		Employee replacement = buildEmployeeWithRoles("repl-1", false, 10, Collections.singleton(Role.STOREKEEPER));
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("mgr-5", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.substituteEmployee(hr, shift, original, replacement));
	}

	private static void hard_shiftController_cancellationRequestAndHandleWithSubstitution() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a6", "p");
		Employee original = buildEmployee("orig-2", false, 10);
		Employee replacement = buildEmployee("repl-2", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 27), ShiftType.EVENING, buildEmployee("mgr-6", true, 10), 1, 1);

		controller.addShift(shift);
		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		controller.requestShiftCancellation(original, shift);

		ShiftAssignment request = controller.getCancellationRequests().get(0);
		controller.handleCancellationWithSubstitution(hr, request, replacement);

		assertEquals(1, shift.getAssignments().size(), "After handling cancellation, shift should still have one assignment");
		assertEquals("repl-2", shift.getAssignments().get(0).getEmployee().getId(), "Replacement employee should be assigned");
	}

	private static void hard_shiftController_recalculateSalaryFromApprovedAssignments() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a7", "p");
		Employee employee = buildEmployee("salary-1", false, 10);
		Shift morning = new Shift(LocalDate.of(2026, 4, 28), ShiftType.MORNING, buildEmployee("mgr-7", true, 10), 1, 1);
		Shift evening = new Shift(LocalDate.of(2026, 4, 29), ShiftType.EVENING, buildEmployee("mgr-8", true, 10), 1, 1);

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
		Salary salary = new Salary(5000, 50, 0, EmploymentScope.FULL_TIME);
		EmploymentTerms terms = new EmploymentTerms(
			LocalDate.of(2024, 1, 1),
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
			new WeeklyAvailabilityRequest()
		);
	}

	private static Employee buildEmployeeWithRoles(String id, boolean canManageShift, int vacationDays, Set<Role> roles) {
		Salary salary = new Salary(5000, 50, 0, EmploymentScope.FULL_TIME);
		EmploymentTerms terms = new EmploymentTerms(
			LocalDate.of(2024, 1, 1),
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
			new WeeklyAvailabilityRequest()
		);
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

	private static void assertEquals(Object expected, Object actual, String message) {
		if (!Objects.equals(expected, actual)) {
			throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
		}
	}

	private static void assertEquals(double expected, double actual, double delta, String message) {
		if (Math.abs(expected - actual) > delta) {
			throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
		}
	}

	private static void assertEquals(int expected, int actual, String message) {
		if (expected != actual) {
			throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}
}
