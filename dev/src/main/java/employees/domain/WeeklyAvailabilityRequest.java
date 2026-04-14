package employees.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeeklyAvailabilityRequest {
    private List<Constraint> constraints;
    private List<Preference> preferences;

    public WeeklyAvailabilityRequest() {
        this.constraints = new ArrayList<>();
        this.preferences = new ArrayList<>();
    }

    public WeeklyAvailabilityRequest(List<Constraint> constraints, List<Preference> preferences) {
        this.constraints = new ArrayList<>(constraints != null ? constraints : Collections.emptyList());
        this.preferences = new ArrayList<>(preferences != null ? preferences : Collections.emptyList());
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

    @Override
    public String toString() {
        return "WeeklyAvailabilityRequest{" +
            "constraints=" + constraints +
            ", preferences=" + preferences +
            '}';
    }
}
