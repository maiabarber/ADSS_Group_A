package dataaccess.mapper;

import dataaccess.dto.SiteDto;
import transportation.domain.ShippingZone;
import transportation.domain.Site;
import transportation.domain.SiteType;

public final class SiteMapper {
    private SiteMapper() {}

    public static SiteDto toDto(Site site) {
        if (site == null) {
            return null;
        }
        return new SiteDto(
                0,
                site.getSiteName(),
                site.getAddress(),
                site.getContactName(),
                site.getPhoneNumber(),
                site.getShippingZone().getZoneCode(),
                site.getSiteType().name()
        );
    }

    public static Site toDomain(SiteDto dto) {
        if (dto == null) {
            return null;
        }
        ShippingZone zone = new ShippingZone(dto.getZoneCode(), dto.getZoneCode());
        SiteType type = dto.getSiteType() == null || dto.getSiteType().isBlank()
                ? SiteType.REGULAR
                : SiteType.valueOf(dto.getSiteType());
        return new Site(
                dto.getSiteName(),
                dto.getAddress(),
                dto.getPhoneNumber(),
                dto.getContactName(),
                zone,
                type,
                null
        );
    }
}
