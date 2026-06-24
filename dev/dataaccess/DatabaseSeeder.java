package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// This class inserts simple sample data into the database.
public class DatabaseSeeder {

    private DatabaseSeeder() {
        // Utility class, no objects needed.
    }

    public static void seedSampleData() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            seedEmployees(statement);
            seedTransportation(statement);
        }
    }

    private static void seedEmployees(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO users (user_id, password, is_hr_manager)
                VALUES
                ('100000001', '1234', 1),
                ('100000002', '1234', 0),
                ('100000003', '1234', 0)
                """);

        statement.execute("""
                INSERT OR IGNORE INTO branches (branch_id, branch_name, address)
                VALUES
                (1, 'Beer Sheva', 'Rager 1, Beer Sheva'),
                (2, 'Tel Aviv', 'Dizengoff 10, Tel Aviv')
                """);

        statement.execute("""
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
                VALUES
                ('100000001', 'HR Manager', '111-111', 'GLOBAL', 'FULL_TIME', 0, 12000, '2026-01-01', 0, 10, 1),
                ('100000002', 'Store Keeper', '222-222', 'HOURLY', 'FULL_TIME', 45, 0, '2026-01-01', 0, 10, 1),
                ('100000003', 'Driver Employee', '333-333', 'HOURLY', 'FULL_TIME', 50, 0, '2026-01-01', 0, 10, NULL)
                """);

        statement.execute("""
                INSERT OR IGNORE INTO employee_roles (employee_id, role_name)
                VALUES
                ('100000001', 'HR_MANAGER'),
                ('100000002', 'STOREKEEPER'),
                ('100000003', 'DRIVER')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO shifts (shift_id, shift_date, shift_type, branch_id)
                VALUES
                (1, '2026-07-01', 'MORNING', 1),
                (2, '2026-07-01', 'EVENING', 1)
                """);

        statement.execute("""
                INSERT OR IGNORE INTO shift_assignments (assignment_id, shift_id, employee_id, role_name, status)
                VALUES
                (1, 1, '100000002', 'STOREKEEPER', 'APPROVED'),
                (2, 1, '100000003', 'DRIVER', 'APPROVED')
                """);
    }

    private static void seedTransportation(Statement statement) throws SQLException {
        statement.execute("""
                INSERT OR IGNORE INTO shipping_zones (zone_code, zone_name)
                VALUES
                ('SOUTH', 'South Zone'),
                ('CENTER', 'Center Zone')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO sites (
                    site_id,
                    site_name,
                    address,
                    contact_name,
                    phone_number,
                    zone_code,
                    site_type
                )
                VALUES
                (1, 'Beer Sheva Branch', 'Rager 1, Beer Sheva', 'Avi Cohen', '050-1111111', 'SOUTH', 'BRANCH'),
                (2, 'South Supplier', 'Industrial Area, Beer Sheva', 'Dana Levi', '050-2222222', 'SOUTH', 'SUPPLIER'),
                (3, 'Tel Aviv Branch', 'Dizengoff 10, Tel Aviv', 'Noa Israel', '050-3333333', 'CENTER', 'BRANCH')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO trucks (
                    license_number,
                    model,
                    net_weight,
                    max_allowed_weight,
                    required_license_type
                )
                VALUES
                ('123-45-678', 'Volvo FH', 8000, 18000, 'C'),
                ('987-65-432', 'Isuzu NPR', 3500, 7500, 'C1')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO drivers (employee_id, driver_name)
                VALUES
                ('100000003', 'Driver Employee')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO driver_license_types (employee_id, license_type)
                VALUES
                ('100000003', 'C'),
                ('100000003', 'C1')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO deliveries (
                    delivery_id,
                    delivery_date,
                    source_site_id,
                    departure_time,
                    final_measured_weight,
                    truck_license_number,
                    driver_employee_id,
                    zone_code,
                    status
                )
                VALUES
                (1, '2026-07-01', 2, '08:00', 9000, '123-45-678', '100000003', 'SOUTH', 'PLANNED')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO delivery_stops (
                    stop_id,
                    delivery_id,
                    stop_order,
                    stop_type,
                    site_id,
                    planned_arrival
                )
                VALUES
                (1, 1, 1, 'PICKUP', 2, '2026-07-01T08:30'),
                (2, 1, 2, 'DROPOFF', 1, '2026-07-01T10:00')
                """);

        statement.execute("""
                INSERT OR IGNORE INTO delivery_documents (document_number, stop_id)
                VALUES
                (1, 1)
                """);

        statement.execute("""
                INSERT OR IGNORE INTO delivery_items (item_id, document_number, item_name, quantity)
                VALUES
                ('ITEM-1', 1, 'Milk Boxes', 40),
                ('ITEM-2', 1, 'Bread Crates', 25)
                """);

        statement.execute("""
                INSERT OR IGNORE INTO delivery_form_measurements (measurement_id, delivery_id, measured_weight)
                VALUES
                (1, 1, 9000)
                """);
    }
}