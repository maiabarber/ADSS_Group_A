package employee.domain;

import transportation.domain.Site;
import java.util.Objects;

/**
 * Branch class represents a company branch/location where employees work.
 * Each branch has its own delivery stop (Site) for deliveries.
 */
public class Branch {
    private String branchId;
    private String branchName;
    private String location;
    private Site Site;

    public Branch(String branchId, String branchName, String location) {
        this(branchId, branchName, location, null);
    }

    public Branch(String branchId, String branchName, String location, Site Site) {
        validateBranchId(branchId);
        validateBranchName(branchName);
        validateLocation(location);
        if (Site != null && Site.getSiteType() != transportation.domain.SiteType.BRANCH) {
            throw new IllegalArgumentException("Branch delivery stop must be a branch site");
        }
        this.branchId = branchId;
        this.branchName = branchName;
        this.location = location;
        this.Site = Site;
        if (this.Site != null) {
            this.Site.setBranch(this);
        }
    }

    private static void validateBranchId(String branchId) {
        if (branchId == null || branchId.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch ID cannot be empty");
        }
    }

    private static void validateBranchName(String branchName) {
        if (branchName == null || branchName.trim().isEmpty()) {
            throw new IllegalArgumentException("Branch name cannot be empty");
        }
        if (branchName.length() > 100) {
            throw new IllegalArgumentException("Branch name cannot exceed 100 characters");
        }
    }

    private static void validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        validateBranchName(branchName);
        this.branchName = branchName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        validateLocation(location);
        this.location = location;
    }

    public Site getSite() {
        return Site;
    }

    public void setSite(Site Site) {
        if (Site != null && Site.getSiteType() != transportation.domain.SiteType.BRANCH) {
            throw new IllegalArgumentException("Branch delivery stop must be a branch site");
        }
        this.Site = Site;
        if (this.Site != null) {
            this.Site.setBranch(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(branchId, branch.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }

    @Override
    public String toString() {
        return branchName + " (" + location + ")";
    }
}
