package dataaccess.mapper;

import dataaccess.dto.PreferenceDto;
import employee.domain.Preference;

public class PreferenceMapper {
    public static Preference toDomain(PreferenceDto preferenceDto) {
        if (preferenceDto == null) return null;
        return new Preference(
                preferenceDto.getDayOfWeek(),
                preferenceDto.getShiftType()
        );
    }

    public static PreferenceDto toDto(Preference preference) {
        if (preference == null) return null;
        return new PreferenceDto(
                preference.getDay(),
                preference.getShiftType()
        );
    }

}
