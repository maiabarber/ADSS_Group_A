# README - Employee Scheduling and Delivery Management Modules

## Submitted By

| Name           | ID        |
| -------------- | --------- |
| Eden Jabarin   | 315259747 |
| Yuval Pariente | 318885936 |
| Maia Berber    | 324431220 |

## Modeling Tools

The following modeling tools were used during the assignment:

* draw.io
* sequenceDiagram

---

## 1. System Overview

This document contains the user instructions for the Employee Scheduling module and the Delivery Management module.
It also describes the initial database content inserted by `DatabaseSeeder.seedSampleData()`.

The system is console-based. All actions are performed by typing the relevant menu number and pressing Enter.

The system supports the following user types:

* HR manager
* Employee
* Transportation manager

The sample database contains demo employees, roles, shifts, transportation entities, one planned delivery, stops, documents, items, and a delivery weight measurement.

---

## 2. Requirements

To run the system, the following are required:

* Windows operating system
* Java JDK 17 or later
* Command Prompt or PowerShell
* The project folder available locally

---

## 3. Tools and Libraries Used

The following tools and libraries were used in the project:

* Java JDK 17
* Maven
* SQLite database
* sqlite-jdbc 3.45.3.0
* SLF4J API 1.7.36
* SLF4J Simple 1.7.36
* JUnit Jupiter 5.10.2 for tests
* Maven Compiler Plugin 3.11.0
* Maven Surefire Plugin 3.2.5
* Maven JAR Plugin 3.3.0
* Maven Shade Plugin 3.5.3

---

## 4. Project Paths and Running the Program

| Item            | Details                      |
| --------------- | ---------------------------- |
| Root folder     | `ADSS_Group_A`               |
| Release folder  | `ADSS_Group_A/release`       |
| Executable file | `adss2025_v02.jar`           |
| Run command     | `java -jar adss2025_v02.jar` |

### Recommended Running Steps

1. Download the project ZIP file.
2. Extract the ZIP file.
3. Open Command Prompt or PowerShell.
4. Navigate to the release folder:

```bash
cd ADSS_Group_A/release
```

5. Run the program:

```bash
java -jar adss2025_v02.jar
```

6. Use the terminal for all input.

---

## 5. Initial Login Users

According to the initial database seed code, the following users are inserted into the `users` table:

| User ID   | Password | User Type              |
| --------- | -------- | ---------------------- |
| 100000001 | 1234     | HR manager             |
| 100000002 | 1234     | Employee / Storekeeper |
| 100000003 | 1234     | Employee / Driver      |

---

## 6. Employee Scheduling Module

The Employee Scheduling module is used for managing employees, roles, shifts, shift assignments, employee constraints, and HR-related actions.

### 6.1 HR Manager Permissions

The HR manager can perform the following actions:

* Set a weekly submission deadline
* Add a new employee
* Update employee details
* Fire an employee
* Approve an employee as a shift manager
* Create a shift
* Assign an employee to a shift
* Substitute an employee in a shift
* Handle cancellation requests
* Calculate employee salary from shifts
* Logout and exit

### 6.2 Employee Permissions

An employee can perform the following actions:

* Submit weekly constraints and preferences
* View and respond to pending shift assignments
* Request shift cancellation
* Transfer a cancellation card if assigned as shift manager
* Logout and exit

### 6.3 General Employee Module Rules

* User ID must contain exactly 9 digits.
* Password cannot be empty.
* Menu selection values must match the numeric options shown in the console.

---

## 7. Delivery Management Module

After starting the Delivery Management module, the main menu appears.
Each action is selected by typing the option number and pressing Enter.

| Option | Menu Action               | Meaning                                                                                 |
| ------ | ------------------------- | --------------------------------------------------------------------------------------- |
| 1      | Initialize empty system   | Reset the system and clear all in-memory transportation data.                           |
| 2      | Load sample data          | Load prepared sample data: deliveries, sites, trucks, drivers, and shipping zones.      |
| 3      | Show all deliveries       | Display all deliveries, including status, date, time, source, driver, truck, and stops. |
| 4      | Show all sites            | Display all sites in the system.                                                        |
| 5      | Show all trucks           | Display all trucks in the system.                                                       |
| 6      | Show all drivers          | Display all drivers in the system.                                                      |
| 7      | Show all shipping zones   | Display all shipping zones.                                                             |
| 8      | Create new delivery       | Create a new delivery by entering the delivery details.                                 |
| 9      | Record weight measurement | Record a weight measurement for an existing delivery.                                   |
| 10     | Replan delivery           | Change delivery planning details such as driver, truck, stops, or items.                |
| 11     | Dispatch delivery         | Mark a delivery as dispatched.                                                          |
| 0      | Exit                      | Close the system.                                                                       |

### 7.1 Creating a New Delivery

When creating a new delivery, the system asks for details such as:

* Delivery date
* Departure time
* Shipping zone
* Source site
* Truck
* Driver
* Stops
* Items

Input formats:

* Date format: `YYYY-MM-DD`
* Time format: `HH:MM`

If an invalid value is entered, the system displays an error message and asks the user to enter the value again.

### 7.2 Recommended First Use

For first use, it is recommended to perform the following steps:

1. Start the system.
2. Choose option `2` - Load sample data.
3. Choose option `3` - View existing deliveries.
4. Choose options `4`-`7` to view sites, trucks, drivers, and shipping zones.
5. Create a new delivery or update an existing delivery as needed.

---

## 8. Initial Database Data

The following tables and rows are inserted by the `DatabaseSeeder` class using `INSERT OR IGNORE` statements.
This means that if a row with the same primary key already exists, it will not be inserted again.

### 8.1 Table: users

| Row | Values                                                    |
| --- | --------------------------------------------------------- |
| 1   | `user_id = 100000001; password = 1234; is_hr_manager = 1` |
| 2   | `user_id = 100000002; password = 1234; is_hr_manager = 0` |
| 3   | `user_id = 100000003; password = 1234; is_hr_manager = 0` |

### 8.2 Table: branches

| Row | Values                                                                    |
| --- | ------------------------------------------------------------------------- |
| 1   | `branch_id = 1; branch_name = Beer Sheva; address = Rager 1, Beer Sheva`  |
| 2   | `branch_id = 2; branch_name = Tel Aviv; address = Dizengoff 10, Tel Aviv` |

### 8.3 Table: employees

| Row | Values                                                                                                                                                                                                                                                        |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `employee_id = 100000001; name = HR Manager; bank_account = 111-111-111111; employment_type = REGULAR; employment_scope = FULL_TIME; hourly_salary = 0; global_salary = 12000; start_date = 2026-01-01; is_fired = 0; vacation_days = 10; branch_id = 1`      |
| 2   | `employee_id = 100000002; name = Store Keeper; bank_account = 222-222-222222; employment_type = REGULAR; employment_scope = FULL_TIME; hourly_salary = 45; global_salary = 0; start_date = 2026-01-01; is_fired = 0; vacation_days = 10; branch_id = 1`       |
| 3   | `employee_id = 100000003; name = Driver Employee; bank_account = 333-333-333333; employment_type = REGULAR; employment_scope = FULL_TIME; hourly_salary = 50; global_salary = 0; start_date = 2026-01-01; is_fired = 0; vacation_days = 10; branch_id = NULL` |

### 8.4 Table: employee_roles

| Row | Values                                             |
| --- | -------------------------------------------------- |
| 1   | `employee_id = 100000001; role_name = CASHIER`     |
| 2   | `employee_id = 100000002; role_name = STOREKEEPER` |
| 3   | `employee_id = 100000003; role_name = DRIVER`      |

### 8.5 Table: shifts

| Row | Values                                                                       |
| --- | ---------------------------------------------------------------------------- |
| 1   | `shift_id = 1; shift_date = 2026-07-01; shift_type = MORNING; branch_id = 1` |
| 2   | `shift_id = 2; shift_date = 2026-07-01; shift_type = EVENING; branch_id = 1` |

### 8.6 Table: shift_assignments

| Row | Values                                                                                                 |
| --- | ------------------------------------------------------------------------------------------------------ |
| 1   | `assignment_id = 1; shift_id = 1; employee_id = 100000002; role_name = STOREKEEPER; status = APPROVED` |
| 2   | `assignment_id = 2; shift_id = 1; employee_id = 100000003; role_name = DRIVER; status = APPROVED`      |

### 8.7 Table: shipping_zones

| Row | Values                                        |
| --- | --------------------------------------------- |
| 1   | `zone_code = SOUTH; zone_name = South Zone`   |
| 2   | `zone_code = CENTER; zone_name = Center Zone` |

### 8.8 Table: sites

| Row | Values                                                                                                                                                                         |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | `site_id = 1; site_name = Beer Sheva Branch; address = Rager 1, Beer Sheva; contact_name = Avi Cohen; phone_number = 050-1111111; zone_code = SOUTH; site_type = BRANCH`       |
| 2   | `site_id = 2; site_name = South Supplier; address = Industrial Area, Beer Sheva; contact_name = Dana Levi; phone_number = 050-2222222; zone_code = SOUTH; site_type = REGULAR` |
| 3   | `site_id = 3; site_name = Tel Aviv Branch; address = Dizengoff 10, Tel Aviv; contact_name = Noa Israel; phone_number = 050-3333333; zone_code = CENTER; site_type = BRANCH`    |

### 8.9 Table: trucks

| Row | Values                                                                                                                     |
| --- | -------------------------------------------------------------------------------------------------------------------------- |
| 1   | `license_number = 123-45-678; model = Volvo FH; net_weight = 8000; max_allowed_weight = 18000; required_license_type = C`  |
| 2   | `license_number = 987-65-432; model = Isuzu NPR; net_weight = 3500; max_allowed_weight = 7500; required_license_type = C1` |

### 8.10 Table: drivers

| Row | Values                                                   |
| --- | -------------------------------------------------------- |
| 1   | `employee_id = 100000003; driver_name = Driver Employee` |

### 8.11 Table: driver_license_types

| Row | Values                                       |
| --- | -------------------------------------------- |
| 1   | `employee_id = 100000003; license_type = C`  |
| 2   | `employee_id = 100000003; license_type = C1` |

### 8.12 Table: deliveries

| Row | Values                                                                                                                                                                                                                          |
| --- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | `delivery_id = 1; delivery_date = 2026-07-01; source_site_id = 2; departure_time = 08:00; final_measured_weight = 9000; truck_license_number = 123-45-678; driver_employee_id = 100000003; zone_code = SOUTH; status = PLANNED` |

### 8.13 Table: delivery_stops

| Row | Values                                                                                                               |
| --- | -------------------------------------------------------------------------------------------------------------------- |
| 1   | `stop_id = 1; delivery_id = 1; stop_order = 1; stop_type = PICKUP; site_id = 2; planned_arrival = 2026-07-01T08:30`  |
| 2   | `stop_id = 2; delivery_id = 1; stop_order = 2; stop_type = DROPOFF; site_id = 1; planned_arrival = 2026-07-01T10:00` |

### 8.14 Table: delivery_documents

| Row | Values                             |
| --- | ---------------------------------- |
| 1   | `document_number = 1; stop_id = 1` |

### 8.15 Table: delivery_items

| Row | Values                                                                           |
| --- | -------------------------------------------------------------------------------- |
| 1   | `item_id = ITEM-1; document_number = 1; item_name = Milk Boxes; quantity = 40`   |
| 2   | `item_id = ITEM-2; document_number = 1; item_name = Bread Crates; quantity = 25` |

### 8.16 Table: delivery_form_measurements

| Row | Values                                                        |
| --- | ------------------------------------------------------------- |
| 1   | `measurement_id = 1; delivery_id = 1; measured_weight = 9000` |

---

## 9. Important:

* Option `1` in the Delivery Management module clears the transportation data from memory. Use it only when you want to work with an empty system.
* Option `2` is recommended for first use because it loads sample data and allows the user to test the main features immediately.
* The initial database driver is employee `100000003`. This employee also appears in the `employees` table and has the `DRIVER` role.
* The planned delivery in the initial database uses truck `123-45-678` and driver employee `100000003`.
* To exit correctly, choose option `0` from the relevant menu.
