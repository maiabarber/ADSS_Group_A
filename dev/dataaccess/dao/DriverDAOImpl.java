package dataaccess.dao;

import dataaccess.dto.DriverDto;
import dataaccess.repository.RepositoryException;
import transportation.domain.LicenseType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DriverDAOImpl implements DaoInterface<DriverDto> {

    private final Connection connection;

    public DriverDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void createOrUpdate(DriverDto driver) throws RepositoryException {
        String insertDriver = """
                INSERT OR REPLACE INTO drivers (
                    employee_id,
                    driver_name
                )
                VALUES (?, ?)
                """;

        String deleteLicenses =
                "DELETE FROM driver_license_types WHERE employee_id = ?";

        String insertLicense = """
                INSERT INTO driver_license_types (
                    employee_id,
                    license_type
                )
                VALUES (?, ?)
                """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement driverStmt =
                         connection.prepareStatement(insertDriver)) {

                driverStmt.setString(1, driver.getEmployeeId());
                driverStmt.setString(2, driver.getDriverName());
                driverStmt.executeUpdate();
            }

            try (PreparedStatement deleteStmt =
                         connection.prepareStatement(deleteLicenses)) {

                deleteStmt.setString(1, driver.getEmployeeId());
                deleteStmt.executeUpdate();
            }

            try (PreparedStatement licenseStmt =
                         connection.prepareStatement(insertLicense)) {

                for (LicenseType licenseType : driver.getLicenseTypes()) {
                    licenseStmt.setString(1, driver.getEmployeeId());
                    licenseStmt.setString(2, licenseType.name());
                    licenseStmt.addBatch();
                }

                licenseStmt.executeBatch();
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }

            throw new RepositoryException(
                    "Failed to create or update driver", e);

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public DriverDto findbyId(String id) throws RepositoryException {
        String sql = """
                SELECT
                    d.employee_id,
                    d.driver_name,
                    l.license_type
                FROM drivers d
                LEFT JOIN driver_license_types l
                    ON d.employee_id = l.employee_id
                WHERE d.employee_id = ?
                """;

        try (PreparedStatement stmt =
                     connection.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {

                String driverName = null;
                Set<LicenseType> licenses = new HashSet<>();

                while (rs.next()) {
                    if (driverName == null) {
                        driverName = rs.getString("driver_name");
                    }

                    String license =
                            rs.getString("license_type");

                    if (license != null) {
                        licenses.add(
                                LicenseType.valueOf(license));
                    }
                }

                if (driverName == null) {
                    return null;
                }

                return new DriverDto(
                        id,
                        driverName,
                        licenses
                );
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to find driver " + id, e);
        }
    }

    @Override
    public void update(DriverDto driver)
            throws RepositoryException {
        createOrUpdate(driver);
    }

    @Override
    public void delete(String id)
            throws RepositoryException {

        String deleteLicenses =
                "DELETE FROM driver_license_types WHERE employee_id = ?";

        String deleteDriver =
                "DELETE FROM drivers WHERE employee_id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt =
                         connection.prepareStatement(deleteLicenses)) {

                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt =
                         connection.prepareStatement(deleteDriver)) {

                stmt.setString(1, id);
                stmt.executeUpdate();
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {
            }

            throw new RepositoryException(
                    "Failed to delete driver " + id, e);

        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    @Override
    public List<DriverDto> findAll()
            throws RepositoryException {

        List<DriverDto> drivers = new ArrayList<>();

        String sql = "SELECT employee_id FROM drivers";

        try (PreparedStatement stmt =
                     connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DriverDto driver =
                        findbyId(rs.getString("employee_id"));

                if (driver != null) {
                    drivers.add(driver);
                }
            }

            return drivers;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to retrieve drivers", e);
        }
    }

    
}