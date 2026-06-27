package dataaccess.mapper;

import dataaccess.dto.SiteDto;
import transportation.domain.Site;

public class SiteMapper {
    private int siteId=0;

    public static SiteDto toDto(Site site) {
    if (site == null) return null;

    return new SiteDto(
            null,
            site.getSiteName(),
            site.getAddress(),
            site.getContactName(),
            site.getPhoneNumber(),
            ShippingZoneMapper.toDto(site.getShippingZone()),
            site.getSiteType(),
            BranchMapper.toDto(site.getBranch())
    );
}

    public static Site toDomain(SiteDto siteDto) {
        return new Site(
            siteDto.getSiteName(),
            siteDto.getAddress(),
            siteDto.getPhoneNumber(),
            siteDto.getContactName(),
            ShippingZoneMapper.toDomain(siteDto.getShippingZone()),
            siteDto.getSiteType(),
            BranchMapper.toDomain(siteDto.getBranch())
        );
    }

}
