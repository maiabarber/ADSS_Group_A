package dataaccess.mapper;

import dataaccess.dto.ShippingZoneDto;
import transportation.domain.ShippingZone;

public final class ShippingZoneMapper {
    private ShippingZoneMapper() {}

    public static ShippingZone toDomain(ShippingZoneDto dto) {
        if (dto == null) {
            return null;
        }
        return new ShippingZone(dto.getZoneCode(), dto.getZoneName());
    }

    public static ShippingZoneDto toDto(ShippingZone zone) {
        if (zone == null) {
            return null;
        }
        return new ShippingZoneDto(zone.getZoneCode(), zone.getZoneName());
    }
}
