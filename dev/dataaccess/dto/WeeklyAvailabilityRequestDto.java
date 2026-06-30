package dataaccess.dto;

public final class WeeklyAvailabilityRequestDto {
    private final int requestId;
    private final String employeeId;
    private final String weekStartDate;
    private final String submissionDeadline;

    public WeeklyAvailabilityRequestDto(int requestId, String employeeId, String weekStartDate, String submissionDeadline) {
        this.requestId = requestId;
        this.employeeId = employeeId;
        this.weekStartDate = weekStartDate;
        this.submissionDeadline = submissionDeadline;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getWeekStartDate() {
        return weekStartDate;
    }

    public String getSubmissionDeadline() {
        return submissionDeadline;
    }

}