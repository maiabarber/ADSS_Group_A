package dataaccess.mapper;


import dataaccess.dto.WeeklyAvailabilityRequestDto;
import employee.domain.WeeklyAvailabilityRequest;

public class WeeklyAvailabilityRequestMapper {

    public static WeeklyAvailabilityRequest toDomain(WeeklyAvailabilityRequestDto weeklyAvailabilityRequest) {
        if (weeklyAvailabilityRequest == null) return null;
        return new WeeklyAvailabilityRequest(
                weeklyAvailabilityRequest.getConstraints().stream()
                        .map(ConstraintMapper::toDomain)
                        .toList(),
                weeklyAvailabilityRequest.getPreferences().stream()
                        .map(PreferenceMapper::toDomain)
                        .toList(),
                weeklyAvailabilityRequest.getSubmissionDeadline(),
                weeklyAvailabilityRequest.getWeekStartDate()
        );
    }

    public static WeeklyAvailabilityRequestDto toDto(WeeklyAvailabilityRequest weeklyAvailabilityRequest) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toDto'");
    }

}
