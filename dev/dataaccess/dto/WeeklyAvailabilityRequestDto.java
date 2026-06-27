package dataaccess.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeeklyAvailabilityRequestDto {
    private final LocalDate weekStartDate;
    private final LocalDate submissionDeadline;
    private final List<ConstraintDto> constraints;
    private final List<PreferenceDto> preferences;

    public WeeklyAvailabilityRequestDto(
            LocalDate weekStartDate,
            LocalDate submissionDeadline,
            List<ConstraintDto> constraints,
            List<PreferenceDto> preferences) {
        this.weekStartDate = weekStartDate;
        this.submissionDeadline = submissionDeadline;
        this.constraints = constraints == null ? new ArrayList<>() : new ArrayList<>(constraints);
        this.preferences = preferences == null ? new ArrayList<>() : new ArrayList<>(preferences);
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public LocalDate getSubmissionDeadline() {
        return submissionDeadline;
    }

    public List<ConstraintDto> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public List<PreferenceDto> getPreferences() {
        return Collections.unmodifiableList(preferences);
    }

}