package dataaccess.mapper;

import dataaccess.dto.WeeklyAvailabilityRequestDto;
import employee.domain.WeeklyAvailabilityRequest;

public final class WeeklyAvailabilityRequestMapper {
    private WeeklyAvailabilityRequestMapper() {}

    public static WeeklyAvailabilityRequest toDomain(WeeklyAvailabilityRequestDto dto) {
        return new WeeklyAvailabilityRequest();
    }
}
