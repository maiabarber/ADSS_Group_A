package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.CreateEmployeeDTO;
import dataaccess.dto.BankAccountDto;
import dataaccess.dto.EmployeeDto;
import dataaccess.dto.EmploymentTermsDto;
import dataaccess.dto.SalaryDto;


import employee.domain.EmploymentScope;
import employee.domain.EmploymentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO {
	public List<EmployeeDto> listEmployees() throws SQLException {
		String sql = """
				SELECT employee_id, name, bank_account, employment_type, employment_scope, hourly_salary, global_salary, start_date, is_fired, vacation_days
				FROM employees
				ORDER BY employee_id
				""";

		List<EmployeeDto> employees = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				employees.add(mapEmployee(resultSet));
			}
		}

		return employees;
	}

	public Optional<EmployeeDto> findEmployeeById(String employeeId) throws SQLException {
		String sql = """
				SELECT employee_id, name, bank_account, employment_type, employment_scope, hourly_salary, global_salary, start_date, is_fired, vacation_days
				FROM employees
				WHERE employee_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, employeeId);
			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(mapEmployee(resultSet));
				}
			}
		}

		return Optional.empty();
	}

	public void insertEmployee(CreateEmployeeDTO employee) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO employees (
				    employee_id,
				    name,
				    bank_account,
				    employment_type,
				    employment_scope,
				    hourly_salary,
				    global_salary,
				    start_date,
				    is_fired,
				    vacation_days,
				    branch_id
				)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, employee.getEmployeeId());
			statement.setString(2, employee.getName());
			statement.setString(3, employee.getBankAccount());
			statement.setString(4, employee.getEmploymentType());
			statement.setString(5, employee.getEmploymentScope());
			statement.setDouble(6, employee.getHourlySalary());
			statement.setDouble(7, employee.getGlobalSalary());
			statement.setString(8, employee.getStartDate());
			statement.setInt(9, employee.isFired() ? 1 : 0);
			statement.setInt(10, employee.getVacationDays());

			if (employee.getBranchId() == null) {
				statement.setNull(11, Types.INTEGER);
			} else {
				statement.setInt(11, employee.getBranchId());
			}

			statement.executeUpdate();
		}
	}

	public void deleteEmployeeById(String employeeId) throws SQLException {
		String sql = "DELETE FROM employees WHERE employee_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, employeeId);
			statement.executeUpdate();
		}
	}

	public int countEmployees() throws SQLException {
		String sql = "SELECT COUNT(*) AS total FROM employees";

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			return resultSet.next() ? resultSet.getInt("total") : 0;
		}
	}

	private EmployeeDto mapEmployee(ResultSet resultSet) throws SQLException {
		return new EmployeeDto(
				resultSet.getString("employee_id"),
				resultSet.getString("name"),
				parseBankAccount(resultSet.getString("bank_account")),
				new SalaryDto(
						resultSet.getDouble("global_salary"),
						resultSet.getDouble("hourly_salary"),
						0,
						parseEmploymentScope(resultSet.getString("employment_scope"))
				),
				parseEmploymentType(resultSet.getString("employment_type")),
				new EmploymentTermsDto(
						parseLocalDate(resultSet.getString("start_date")),
						parseEmploymentScope(resultSet.getString("employment_scope")),
						resultSet.getDouble("global_salary"),
						resultSet.getDouble("hourly_salary"),
						resultSet.getInt("vacation_days")
				),
				Collections.emptySet(),
				false,
				resultSet.getInt("is_fired") == 1,
				null,
				null,
				null
		);
	}

	private static BankAccountDto parseBankAccount(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return null;
		}

		String[] parts = rawValue.split("-");
		if (parts.length == 3) {
			return new BankAccountDto(parts[0], parts[1], parts[2]);
		}

		return new BankAccountDto(rawValue, null, null);
	}

	private static EmploymentScope parseEmploymentScope(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return EmploymentScope.FULL_TIME;
		}
		return EmploymentScope.valueOf(rawValue);
	}

	private static EmploymentType parseEmploymentType(String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return EmploymentType.REGULAR;
		}
		return EmploymentType.valueOf(rawValue);
	}

	private static LocalDate parseLocalDate(String rawValue) {
		return rawValue == null || rawValue.isBlank() ? null : LocalDate.parse(rawValue);
	}
}