package dataaccess.dao;

import dataaccess.DatabaseConnection;
import dataaccess.dto.ShiftAssignmentDto;
import dataaccess.dto.ShiftDto;

import employee.domain.Role;
import employee.domain.ShiftType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.List;

public class ShiftDAO {
	public List<ShiftDto> listShifts() throws SQLException {
		String sql = """
				SELECT shift_id, shift_date, shift_type, branch_id
				FROM shifts
				ORDER BY shift_id
				""";

		List<ShiftDto> shifts = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				shifts.add(new ShiftDto(
						LocalDate.parse(resultSet.getString("shift_date")),
						ShiftType.valueOf(resultSet.getString("shift_type")),
						null,
						null,
						null,
						null,
						false,
						null
				));
			}
		}

		return shifts;
	}

	public Optional<ShiftDto> findShiftById(int shiftId) throws SQLException {
		String sql = """
				SELECT shift_id, shift_date, shift_type, branch_id
				FROM shifts
				WHERE shift_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, shiftId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new ShiftDto(
						LocalDate.parse(resultSet.getString("shift_date")),
						ShiftType.valueOf(resultSet.getString("shift_type")),
						null,
						null,
						null,
						null,
						false,
						null
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertShift(int shiftId,
	                        String shiftDate,
	                        String shiftType,
	                        Integer branchId) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO shifts (
				    shift_id,
				    shift_date,
				    shift_type,
				    branch_id
				)
				VALUES (?, ?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, shiftId);
			statement.setString(2, shiftDate);
			statement.setString(3, shiftType);

			if (branchId == null) {
				statement.setNull(4, java.sql.Types.INTEGER);
			} else {
				statement.setInt(4, branchId);
			}

			statement.executeUpdate();
		}
	}

	public void deleteShiftById(int shiftId) throws SQLException {
		String sql = "DELETE FROM shifts WHERE shift_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, shiftId);
			statement.executeUpdate();
		}
	}

	public List<ShiftAssignmentDto> listShiftAssignments() throws SQLException {
		String sql = """
				SELECT employee_id, shift_id, role_name, status
				FROM shift_assignments
				ORDER BY assignment_id
				""";

		List<ShiftAssignmentDto> assignments = new ArrayList<>();

		try (Connection connection = DatabaseConnection.getConnection();
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(sql)) {

			while (resultSet.next()) {
				assignments.add(new ShiftAssignmentDto(
						resultSet.getString("employee_id"),
						null,
						null,
						Role.valueOf(resultSet.getString("role_name")),
						"APPROVED".equalsIgnoreCase(resultSet.getString("status")),
						!"APPROVED".equalsIgnoreCase(resultSet.getString("status")),
						false,
						false
				));
			}
		}

		return assignments;
	}

	public Optional<ShiftAssignmentDto> findShiftAssignmentById(int assignmentId) throws SQLException {
		String sql = """
				SELECT employee_id, shift_id, role_name, status
				FROM shift_assignments
				WHERE assignment_id = ?
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, assignmentId);

			try (ResultSet resultSet = statement.executeQuery()) {
				if (resultSet.next()) {
					return Optional.of(new ShiftAssignmentDto(
						resultSet.getString("employee_id"),
						null,
						null,
						Role.valueOf(resultSet.getString("role_name")),
						"APPROVED".equalsIgnoreCase(resultSet.getString("status")),
						!"APPROVED".equalsIgnoreCase(resultSet.getString("status")),
						false,
						false
					));
				}
			}
		}

		return Optional.empty();
	}

	public void insertShiftAssignment(int assignmentId,
	                                 int shiftId,
	                                 String employeeId,
	                                 String roleName,
	                                 String status) throws SQLException {
		String sql = """
				INSERT OR IGNORE INTO shift_assignments (
				    assignment_id,
				    shift_id,
				    employee_id,
				    role_name,
				    status
				)
				VALUES (?, ?, ?, ?, ?)
				""";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, assignmentId);
			statement.setInt(2, shiftId);
			statement.setString(3, employeeId);
			statement.setString(4, roleName);
			statement.setString(5, status);
			statement.executeUpdate();
		}
	}

	public void deleteShiftAssignmentById(int assignmentId) throws SQLException {
		String sql = "DELETE FROM shift_assignments WHERE assignment_id = ?";

		try (Connection connection = DatabaseConnection.getConnection();
			 PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setInt(1, assignmentId);
			statement.executeUpdate();
		}
	}
}