package employees.presentation;

import employees.domain.Constraint;
import employees.domain.Preference;
import employees.domain.WeeklyAvailabilityRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AvailabilityPresentation {
    private List<Constraint> constraintsInput = new ArrayList<>();
    private List<Preference> preferencesInput = new ArrayList<>();
    private LocalDate submissionDeadlineInput;

    public WeeklyAvailabilityRequest toWeeklyAvailabilityRequest() {
        return new WeeklyAvailabilityRequest(
            constraintsInput,
            preferencesInput,
            Objects.requireNonNull(submissionDeadlineInput, "submissionDeadlineInput must not be null")
        );
    }

    public List<Constraint> getConstraintsInput() {
        return constraintsInput;
    }

    public void setConstraintsInput(List<Constraint> constraintsInput) {
        this.constraintsInput = new ArrayList<>(constraintsInput != null ? constraintsInput : new ArrayList<Constraint>());
    }

    public List<Preference> getPreferencesInput() {
        return preferencesInput;
    }

    public void setPreferencesInput(List<Preference> preferencesInput) {
        this.preferencesInput = new ArrayList<>(preferencesInput != null ? preferencesInput : new ArrayList<Preference>());
    }

    public LocalDate getSubmissionDeadlineInput() {
        return submissionDeadlineInput;
    }

    public void setSubmissionDeadlineInput(LocalDate submissionDeadlineInput) {
        this.submissionDeadlineInput = submissionDeadlineInput;
    }
}
