package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// This class creates the database tables if they do not already exist.
public class DatabaseInitializer {

    private DatabaseInitializer() {
        // Utility class, no objects needed.
    }

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");

            createEmployeeTables(statement);
            createTransportationTables(statement);
        }
    }

    private static void createEmployeeTables(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    is_hr_manager INTEGER NOT NULL DEFAULT 0
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                    branch_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    branch_name TEXT NOT NULL UNIQUE,
                    address TEXT,
                    delivery_stop_site_id INTEGER,
                    FOREIGN KEY (delivery_stop_site_id) REFERENCES sites(site_id)
                )
                """);
                
        statement.execute("""
                CREATE TABLE IF NOT EXISTS employees (
                    employee_id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    bank_number TEXT,
                    bank_branch_number TEXT,
                    bank_account_number TEXT,
                    employment_type TEXT,
                    employment_scope TEXT,
                    hourly_salary REAL,
                    global_salary REAL,
                    start_date TEXT,
                    is_fired INTEGER NOT NULL DEFAULT 0,
                    vacation_days INTEGER NOT NULL DEFAULT 10,
                    branch_id INTEGER,
                    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS employee_roles (
                    employee_id TEXT NOT NULL,
                    role_name TEXT NOT NULL,
                    PRIMARY KEY (employee_id, role_name),
                    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS shifts (
                    shift_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    shift_date TEXT NOT NULL,
                    shift_type TEXT NOT NULL,
                    branch_id INTEGER,
                    FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS shift_assignments (
                    assignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    shift_id INTEGER NOT NULL,
                    employee_id TEXT NOT NULL,
                    role_name TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (shift_id) REFERENCES shifts(shift_id),
                    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
                )
                """);

        statement.execute("""
            CREATE TABLE IF NOT EXISTS submissiondeadlines (
                deadline_date TEXT NOT NULL
            )
            """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS weeklyavailabilityrequests (
                    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    employee_id TEXT NOT NULL,
                    week_start_date TEXT NOT NULL,
                    submission_deadline TEXT NOT NULL
                )
                """);
        
        statement.execute("""
                CREATE TABLE IF NOT EXISTS weekly_availability_constraints (
                    request_id INTEGER NOT NULL,
                    day_of_week TEXT NOT NULL,
                    shift_type TEXT NOT NULL,
                    PRIMARY KEY (request_id, day_of_week, shift_type),
                    FOREIGN KEY (request_id) REFERENCES weeklyavailabilityrequests(request_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS weekly_availability_preferences (
                    request_id INTEGER NOT NULL,
                    day_of_week TEXT NOT NULL,
                    shift_type TEXT NOT NULL,
                    PRIMARY KEY (request_id, day_of_week, shift_type),
                    FOREIGN KEY (request_id) REFERENCES weeklyavailabilityrequests(request_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS driverassignmentrequests (
                    request_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    driver_id TEXT NOT NULL,
                    delivery_id INTEGER NOT NULL,
                    delivery_date_time TEXT NOT NULL,
                    shift_type TEXT NOT NULL,
                    handled INTEGER NOT NULL DEFAULT 0,
                    status_message TEXT NOT NULL
                )
                """);
    }

    private static void createTransportationTables(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS shipping_zones (
                    zone_code TEXT PRIMARY KEY,
                    zone_name TEXT NOT NULL
                )
                """);

        statement.execute("""
        CREATE TABLE IF NOT EXISTS sites (
            site_id INTEGER PRIMARY KEY AUTOINCREMENT,
            site_name TEXT NOT NULL UNIQUE,
            address TEXT NOT NULL,
            contact_name TEXT NOT NULL,
            phone_number TEXT NOT NULL,
            zone_code TEXT NOT NULL,
            site_type TEXT,
            branch_id INTEGER,
            FOREIGN KEY (zone_code) REFERENCES shipping_zones(zone_code),
            FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
        )
        """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS trucks (
                    license_number TEXT PRIMARY KEY,
                    model TEXT NOT NULL,
                    net_weight REAL NOT NULL,
                    max_allowed_weight REAL NOT NULL,
                    required_license_type TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS drivers (
                    employee_id TEXT PRIMARY KEY,
                    driver_name TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS driver_license_types (
                    employee_id TEXT NOT NULL,
                    license_type TEXT NOT NULL,
                    PRIMARY KEY (employee_id, license_type),
                    FOREIGN KEY (employee_id) REFERENCES drivers(employee_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS deliveries (
                    delivery_id INTEGER PRIMARY KEY,
                    delivery_date TEXT NOT NULL,
                    source_site_id INTEGER NOT NULL,
                    departure_time TEXT NOT NULL,
                    final_measured_weight REAL NOT NULL,
                    truck_license_number TEXT NOT NULL,
                    driver_employee_id TEXT NOT NULL,
                    zone_code TEXT NOT NULL,
                    status TEXT NOT NULL,
                    FOREIGN KEY (source_site_id) REFERENCES sites(site_id),
                    FOREIGN KEY (truck_license_number) REFERENCES trucks(license_number),
                    FOREIGN KEY (driver_employee_id) REFERENCES drivers(employee_id),
                    FOREIGN KEY (zone_code) REFERENCES shipping_zones(zone_code)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS delivery_stops (
                    stop_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    delivery_id INTEGER NOT NULL,
                    stop_order INTEGER NOT NULL,
                    stop_type TEXT NOT NULL,
                    site_id INTEGER NOT NULL,
                    planned_arrival TEXT,
                    FOREIGN KEY (delivery_id) REFERENCES deliveries(delivery_id),
                    FOREIGN KEY (site_id) REFERENCES sites(site_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS delivery_documents (
                    document_number INTEGER PRIMARY KEY,
                    stop_id INTEGER NOT NULL,
                    FOREIGN KEY (stop_id) REFERENCES delivery_stops(stop_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS delivery_items (
                    item_id TEXT NOT NULL,
                    document_number INTEGER NOT NULL,
                    item_name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    PRIMARY KEY (item_id, document_number),
                    FOREIGN KEY (document_number) REFERENCES delivery_documents(document_number)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS delivery_form_measurements (
                    measurement_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    delivery_id INTEGER NOT NULL,
                    measured_weight REAL NOT NULL,
                    FOREIGN KEY (delivery_id) REFERENCES deliveries(delivery_id)
                )
                """);
    }
}
