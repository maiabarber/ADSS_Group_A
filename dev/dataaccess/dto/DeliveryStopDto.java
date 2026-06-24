package dataaccess.dto;

import transportation.domain.StopType;

import java.time.LocalDateTime;

public final class DeliveryStopDto {
    private final int stopOrder;
    private final StopType stopType;
    private final SiteDto site;
    private final LocalDateTime plannedArrival;
    private final DeliveryDocumentDto document;

    public DeliveryStopDto(
            int stopOrder,
            StopType stopType,
            SiteDto site,
            LocalDateTime plannedArrival,
            DeliveryDocumentDto document) {
        this.stopOrder = stopOrder;
        this.stopType = stopType;
        this.site = site;
        this.plannedArrival = plannedArrival;
        this.document = document;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public StopType getStopType() {
        return stopType;
    }

    public SiteDto getSite() {
        return site;
    }

    public LocalDateTime getPlannedArrival() {
        return plannedArrival;
    }

    public DeliveryDocumentDto getDocument() {
        return document;
    }
}