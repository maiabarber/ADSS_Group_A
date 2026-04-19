# Manual End-to-End Tests (Central Processes)

This file describes manual tests for central console flows in the system.
Run the application from the `dev` directory and follow each script exactly.

## Preconditions

- Start from a clean app run.
- Use built-in demo credentials from code:
  - HR manager: `hr001` / `hrpass`
  - Employee: `employee1` / `pass123`

## E2E-1: HR Sets Weekly Deadline + Employee Submits Weekly Availability (Easy)

### Goal
Validate the common weekly process from setup to successful employee submission.

### Steps
1. Start app and login as HR manager (`hr001` / `hrpass`).
2. Choose option `1` (set weekly submission deadline).
3. Enter a future date (for example: next Sunday).
4. Logout.
5. Login as employee (`employee1` / `pass123`).
6. If asked, set fixed day off (for example `SUNDAY`).
7. Choose option `1` (submit weekly constraints and preferences).
8. Enter 1-2 constraints and 1-2 preferences.
9. Enter `0` vacation days to use.

### Expected Result
- Employee gets success message for weekly submission.
- Remaining vacation days are printed and unchanged.
- No authorization or deadline errors are shown.

## E2E-2: Hard Negative Flow - Deadline Passed Blocks Submission

### Goal
Validate hard business rule that availability cannot be submitted after deadline.

### Steps
1. Login as HR manager (`hr001` / `hrpass`).
2. Set weekly submission deadline to a past date (for example yesterday).
3. Logout.
4. Login as employee (`employee1` / `pass123`).
5. Choose option `1` (submit weekly constraints and preferences).

### Expected Result
- System prints that submission deadline has passed.
- Submission is not saved.
- Employee vacation balance is not reduced.

## E2E-3: Hard Authorization Flow - Only HR Can Approve Shift Manager and Assign Shift Manager to Shift

### Goal
Validate role-based authorization and certified-manager constraints in central scheduling flow.

### Steps
1. Login as HR manager (`hr001` / `hrpass`).
2. Add a new employee from option `2` (can manage shift should initially be false if prompted).
3. Try creating a shift and selecting that employee as manager before approval.
4. Observe failure/validation message.
5. Use option `5` to approve the employee as shift manager.
6. Create shift again and select the same employee as manager.
7. Logout.
8. Login as regular employee (`employee1` / `pass123`) and try any HR-only action (for example adding employee).

### Expected Result
- Before approval, candidate cannot be assigned as shift manager.
- After HR approval, assignment succeeds.
- Non-HR user cannot perform HR-only operations.

## E2E-4: Conflict Assignment + Employee Approval (Hard)

### Goal
Validate pending assignment behavior when HR assigns a shift that conflicts with employee availability.

### Steps
1. Login as HR manager (`hr001` / `hrpass`).
2. Set weekly deadline to a future date.
3. Add employee `empA`.
4. Logout and login as `empA`; submit a weekly constraint for a specific day/shift (for example MONDAY MORNING), then logout.
5. Login as HR manager and create that same shift.
6. Assign `empA` to the same role/shift.
7. Observe warning that assignment was sent for employee approval.
8. Logout and login as `empA`; open pending assignments and approve.

### Expected Result
- Assignment is initially pending (not auto-approved).
- Employee can approve it from pending list.
- Shift now includes approved assignment.

## E2E-5: Shift Cancellation + HR Substitution (Hard)

### Goal
Validate cancellation request lifecycle from employee request through HR substitution.

### Steps
1. Login as HR manager and ensure two employees exist for same role (for example `empB`, `empC`).
2. Create a shift and assign `empB`.
3. Logout and login as `empB`; request cancellation for assigned shift.
4. Logout and login as HR manager.
5. Open cancellation requests and handle by substituting `empC`.

### Expected Result
- Cancellation request appears in HR list.
- Substitution succeeds.
- Final shift assignment is on replacement employee (`empC`) and not on original (`empB`).

## E2E-6: Salary Calculation from Approved Shifts (Easy to Medium)

### Goal
Validate that approved shift assignments are counted into worked hours and salary calculation.

### Steps
1. Login as HR manager and create at least 2 shifts in same week.
2. Assign employee `employee1` to both shifts and complete pending approvals if required.
3. Run salary calculation action from HR menu for `employee1`.

### Expected Result
- Worked hours include only approved assignments.
- Salary output is calculated and shown without errors.
- If total hours are below overtime threshold, output should remain close to base salary.

## Notes

- If data persists between runs in your environment, restart and repeat from a clean state.
- Keep console output logs/screenshots for submission evidence.
