package employees.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class WeeklyAvailabilityRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Constraint> constraints;
    private List<Preference> preferences;
    private LocalDate submissionDeadline;
    private LocalDate weekStartDate;

    public WeeklyAvailabilityRequest() {
        this.constraints = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }

    public WeeklyAvailabilityRequest(List<Constraint> constraints, List<Preference> preferences) {
        this.constraints = new ArrayList<>(constraints != null ? constraints : Collections.emptyList());
        this.preferences = new ArrayList<>(preferences != null ? preferences : Collections.emptyList());
    }

    public WeeklyAvailabilityRequest(List<Constraint> constraints, List<Preference> preferences, LocalDate submissionDeadline) {
        this.constraints = new ArrayList<>(constraints != null ? constraints : Collections.emptyList());
        this.preferences = new ArrayList<>(preferences != null ? preferences : Collections.emptyList());
        this.submissionDeadline = Objects.requireNonNull(submissionDeadline, "submissionDeadline must not be null");
    }

    public List<Constraint> getConstraints() {
        return Collections.unmodifiableList(constraints);
    }

    public void setConstraints(List<Constraint> constraints) {
        this.constraints = new ArrayList<>(constraints != null ? constraints : Collections.emptyList());
    }

    public List<Preference> getPreferences() {
        return Collections.unmodifiableList(preferences);
    }

    public void setPreferences(List<Preference> preferences) {
        this.preferences = new ArrayList<>(preferences != null ? preferences : Collections.emptyList());
    }

    public LocalDate getSubmissionDeadline() {
        return submissionDeadline;
    }

    public void setSubmissionDeadline(LocalDate submissionDeadline) {
        this.submissionDeadline = Objects.requireNonNull(submissionDeadline, "submissionDeadline must not be null");
    }

    public LocalDate getWeekStartDate() {
        return weekStartDate;
    }

    public void setWeekStartDate(LocalDate weekStartDate) {
        this.weekStartDate = weekStartDate;
    }

    public void addConstraint(Constraint constraint) {
        if (constraint != null) {
            constraints.add(constraint);
        }
    }

    public void addPreference(Preference preference) {
        if (preference != null) {
            preferences.add(preference);
        }
    }

    public void resetForWeek(LocalDate weekStartDate) {
        this.constraints = new ArrayList<>();
        this.preferences = new ArrayList<>();
        this.weekStartDate = weekStartDate;
    }

    @Override
    public String toString() {
        return "WeeklyAvailabilityRequest{" +
            "constraints=" + constraints +
            ", preferences=" + preferences +
            ", submissionDeadline=" + submissionDeadline +
            ", weekStartDate=" + weekStartDate +
            '}';
    }
}
