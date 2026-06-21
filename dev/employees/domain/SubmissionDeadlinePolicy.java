package employees.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * SubmissionDeadlinePolicy centralizes business rules for weekly submission deadlines.
 */
public class SubmissionDeadlinePolicy {
    public void validateManagerDeadline(LocalDate newDeadline, LocalDate today) {
        if (newDeadline == null) {
            throw new IllegalArgumentException("Deadline is required");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date is required");
        }
        if (newDeadline.isBefore(today)) {
            throw new IllegalArgumentException("Invalid deadline: it cannot be in the past.");
        }

        LocalDate maxAllowedDeadline = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
        if (newDeadline.isAfter(maxAllowedDeadline)) {
            throw new IllegalArgumentException(
                "Invalid deadline: it exceeds this week's."
            );
        }
    }

    public LocalDate resolveOpenSubmissionDeadline(LocalDate configuredDeadline, LocalDate today) {
        if (configuredDeadline == null) {
            throw new IllegalArgumentException("No submission deadline is configured yet. Please contact HR manager.");
        }
        if (today == null) {
            throw new IllegalArgumentException("Current date is required");
        }
        if (today.isAfter(configuredDeadline)) {
            throw new IllegalArgumentException("Submission deadline has passed for this week.");
        }
        return configuredDeadline;
    }
}
