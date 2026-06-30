package dataaccess.dto;

public final class SubmissionDeadlineDto {
    private final String deadlineDate;

    public SubmissionDeadlineDto(String deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public String getDeadlineDate() {
        return deadlineDate;
    }

}