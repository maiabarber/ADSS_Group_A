package dataaccess.dto;

public final class WeeklyAvailabilityPreferenceDto {
    private final int requestId;
    private final String dayOfWeek;
    private final String shiftType;

    public WeeklyAvailabilityPreferenceDto(int requestId, String dayOfWeek, String shiftType) {
        this.requestId = requestId;
        this.dayOfWeek = dayOfWeek;
        this.shiftType = shiftType;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public String getShiftType() {
        return shiftType;
    }

}