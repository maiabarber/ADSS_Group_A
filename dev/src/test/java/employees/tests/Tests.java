package employees.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class Tests {
	@Test
	public void easy_salaryNoOvertime_whenWorkedBelowThreshold() {
		Salary salary = new Salary(5000, 50, 120, EmploymentScope.FULL_TIME);

		assertEquals("Overtime should be zero for full-time under 190h", 0.0, salary.getOvertimeHours(), 0.0001);
		assertEquals("Final salary should equal base when no overtime", 5000.0, salary.getFinalSalary(), 0.0001);
	}

	@Test
	public void easy_salaryOvertime_forPartTimeEmployee() {
		Salary salary = new Salary(3000, 40, 100, EmploymentScope.PART_TIME);

		assertEquals("Part-time overtime should be workedHours - 95", 5.0, salary.getOvertimeHours(), 0.0001);
		assertEquals("Final salary should include overtime pay", 3200.0, salary.getFinalSalary(), 0.0001);
	}

	@Test
	public void easy_salaryRejectsNullEmploymentScope() {
		Salary salary = new Salary(3000, 30, 100);

		assertThrows(IllegalArgumentException.class, () -> salary.setEmploymentScope(null));
	}

	@Test
	public void easy_weeklyAvailabilityReset_clearsData() {
		WeeklyAvailabilityRequest request = new WeeklyAvailabilityRequest();
		request.addConstraint(new employees.domain.Constraint(DayOfWeek.SUNDAY, ShiftType.MORNING));
		request.addPreference(new employees.domain.Preference(DayOfWeek.MONDAY, ShiftType.EVENING));

		LocalDate newWeek = LocalDate.of(2026, 4, 20);
		request.resetForWeek(newWeek);

		assertTrue("Constraints should be cleared after reset", request.getConstraints().isEmpty());
		assertTrue("Preferences should be cleared after reset", request.getPreferences().isEmpty());
		assertEquals("Week start date should be updated", newWeek, request.getWeekStartDate());
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
		Employee employee = buildEmployee("e1", false, 10);

		employee.consumeVacationDays(3);

		assertEquals("Vacation balance should decrease by consumed amount", 7, employee.getVacationDaysBalance());
	}

	@Test
	public void hard_employeeVacationConsumption_rejectsOveruse() {
		Employee employee = buildEmployee("e2", false, 2);

		assertThrows(IllegalArgumentException.class, () -> employee.consumeVacationDays(3));
	}

	@Test
	public void hard_employeeFixedDayOff_canOnlyBeSetOnce() {
		Employee employee = buildEmployee("e3", false, 10);

		employee.setFixedDayOff(DayOfWeek.SUNDAY);
		assertEquals("Fixed day off should be set the first time", DayOfWeek.SUNDAY, employee.getFixedDayOff());

		assertThrows(IllegalStateException.class, () -> employee.setFixedDayOff(DayOfWeek.MONDAY));
	}

	@Test
	public void hard_shiftAssignManager_requiresHrManager() {
		Employee candidate = buildEmployee("m1", true, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new employees.domain.User("u1", "p"), candidate));
	}

	@Test
	public void hard_shiftAssignManager_requiresCertifiedCandidate() {
		Employee notCertified = buildEmployee("m2", false, 10);
		Shift shift = new Shift();

		assertThrows(IllegalArgumentException.class, () -> shift.assignShiftManager(new HR_Manager("hr1", "pass"), notCertified));
	}

	@Test
	public void hard_shiftConfigureRoleCounts_requiresHrManager() {
		Shift shift = new Shift();
		Map<Role, Integer> customCounts = new EnumMap<>(Role.class);
		customCounts.put(Role.CASHIER, 3);

		assertThrows(IllegalArgumentException.class, () -> shift.configureRequiredRoleCounts(new employees.domain.User("u2", "p"), customCounts));
	}

	@Test
	public void hard_shiftTransferCancellationCard_onlyCurrentManager() {
		Employee shiftManager = buildEmployee("manager", true, 10);
		Employee otherEmployee = buildEmployee("other", true, 10);
		Shift shift = new Shift(LocalDate.now(), ShiftType.MORNING, shiftManager, 2, 1);

		assertThrows(IllegalArgumentException.class, () -> shift.transferCancellationCard(otherEmployee));

		shift.transferCancellationCard(shiftManager);
		assertTrue("Cancellation card should be transferred by shift manager", shift.isCancellationCardTransferred());
		assertEquals("Transfer actor should be the shift manager", shiftManager.getId(), shift.getCancellationCardTransferredBy().getId());
		assertNotNull("Transfer time should be recorded", shift.getCancellationCardTransferTime());
	}

	@Test
	public void easy_authenticationLogin_success() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		User user = new User("u-login", "secret");

		try {
			auth.registerUser(user);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertTrue("Login should succeed for valid credentials", auth.login("u-login", "secret").isPresent());
		assertTrue("Auth state should be logged-in after successful login", auth.isLoggedIn());
	}

	@Test
	public void easy_authenticationLogin_blocksFiredEmployee() {
		AuthenticationService auth = new AuthenticationService(new InMemoryUserRepository());
		Employee firedEmployee = buildEmployee("fired-1", false, 10);
		firedEmployee.setFired(true);

		try {
			auth.registerUser(firedEmployee);
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}

		assertFalse("Fired employee should be blocked from login", auth.login("fired-1", "pass").isPresent());
		assertTrue("Fired credentials should be detected", auth.isFiredCredentials("fired-1", "pass"));
	}

	@Test
	public void easy_employeeRepositorySaveFindDelete_cycle() {
		InMemoryEmployeeRepository repository = new InMemoryEmployeeRepository();
		Employee employee = buildEmployee("repo-1", false, 10);

		try {
			repository.save(employee);
			assertTrue("Saved employee should be retrievable by id", repository.findById("repo-1").isPresent());
			repository.deleteById("repo-1");
			assertFalse("Deleted employee should no longer exist", repository.findById("repo-1").isPresent());
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void easy_submissionDeadlineRepository_roundTrip() {
		InMemorySubmissionDeadlineRepository repository = new InMemorySubmissionDeadlineRepository();
		LocalDate deadline = LocalDate.of(2026, 4, 25);

		try {
			repository.save(deadline);
			assertEquals("Saved deadline should be loaded back", deadline, repository.findCurrent().orElse(null));
		} catch (RepositoryException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void hard_shiftController_assignNoConflict_autoApproved() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a1", "p");
		Employee employee = buildEmployee("assign-1", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 21), ShiftType.MORNING, buildEmployee("mgr-1", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);

		assertEquals("Shift should have one assignment", 1, shift.getAssignments().size());
		assertTrue("No-conflict assignment should be auto-approved", shift.getAssignments().get(0).isApproved());
	}

	@Test
	public void hard_shiftController_assignConstraintConflict_pending() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a2", "p");
		Employee employee = buildEmployee("assign-2", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 22), ShiftType.MORNING, buildEmployee("mgr-2", true, 10), 1, 1);

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
		HR_Manager hr = new HR_Manager("hr-v1", "p");
		Employee employee = buildEmployee("vac-1", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 22), ShiftType.MORNING, buildEmployee("mgr-v1", true, 10), 1, 1);

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

		assertEquals("Rejected pending assignment should be removed from shift", 0, shift.getAssignments().size());
	}

	@Test
	public void hard_shiftController_doubleShiftRequiresPreference() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a4", "p");
		Employee employee = buildEmployee("assign-4", false, 10);
		Shift shift = new Shift(LocalDate.of(2026, 4, 24), ShiftType.DOUBLE_SHIFT, buildEmployee("mgr-4", true, 10), 1, 1);

		assertThrows(IllegalArgumentException.class, () -> controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER));

		employee.getWeeklyAvailabilityRequest().addPreference(
			new Preference(shift.getDate().getDayOfWeek(), ShiftType.DOUBLE_SHIFT)
		);
		controller.assignEmployeeToShift(hr, employee, shift, Role.CASHIER);
		assertEquals("Assignment should succeed after matching preference is provided", 1, shift.getAssignments().size());
	}

	@Test
	public void hard_shiftController_substituteRoleMismatch_rejected() {
		ShiftController controller = new ShiftController();
		HR_Manager hr = new HR_Manager("hr-a5", "p");
		Employee original = buildEmployee("orig-1", false, 10);
		Employee replacement = buildEmployeeWithRoles("repl-1", false, 10, Collections.singleton(Role.STOREKEEPER));
		Shift shift = new Shift(LocalDate.of(2026, 4, 26), ShiftType.MORNING, buildEmployee("mgr-5", true, 10), 1, 1);

		controller.assignEmployeeToShift(hr, original, shift, Role.CASHIER);
		assertThrows(IllegalArgumentException.class, () -> controller.substituteEmployee(hr, shift, original, replacement));
	}

	@Test
	public void hard_shiftController_cancellationRequestAndHandleWithSubstitution() {
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

		assertEquals("After handling cancellation, shift should still have one assignment", 1, shift.getAssignments().size());
		assertEquals("Replacement employee should be assigned", "repl-2", shift.getAssignments().get(0).getEmployee().getId());
	}

	@Test
	public void hard_shiftController_recalculateSalaryFromApprovedAssignments() {
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

		assertEquals("Two approved regular shifts should total 16 worked hours", 16.0, workedHours, 0.0001);
		assertEquals("16h is below full-time overtime threshold so salary stays base", 5000.0, finalSalary, 0.0001);
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
}
