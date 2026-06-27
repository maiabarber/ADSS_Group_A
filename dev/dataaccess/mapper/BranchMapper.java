package dataaccess.mapper;

import java.sql.ResultSet;

import dataaccess.dto.BranchDto;
import employee.domain.Branch;

public class BranchMapper {

    public static BranchDto toDto(Branch branch) {
    if (branch == null) return null;

    return new BranchDto(
            branch.getBranchId(),
            branch.getBranchName(),
            branch.getLocation(),
            SiteMapper.toDto(branch.getSite())
    );
}
    public static Branch toDomain(BranchDto dto) {
        if (dto == null) return null;
        return new Branch(
            dto.getBranchId(),
            dto.getBranchName(),
            dto.getLocation(),
            SiteMapper.toDomain(dto.getSite())
        );
    }

}