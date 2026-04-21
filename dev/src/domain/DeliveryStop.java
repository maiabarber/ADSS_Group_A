package domain;

public class DeliveryStop {

    private int stopOrder;
    private StopType stopType;
    private Site site;
    private DeliveryDocument document;

    /**
     * Legacy constructor kept for backward compatibility.
     */
    public DeliveryStop(int stopOrder, StopType stopType, Site site) {
        this(stopOrder, stopType, site, null);
    }

    public DeliveryStop(int stopOrder, StopType stopType, Site site, DeliveryDocument document) {
        validateStopOrder(stopOrder);

        if (stopType == null) {
            throw new IllegalArgumentException("stopType cannot be null");
        }
        if (site == null) {
            throw new IllegalArgumentException("site cannot be null");
        }

        this.stopOrder = stopOrder;
        this.stopType = stopType;
        this.site = site;
        this.document = document;
    }

    public int getStopOrder() {
        return stopOrder;
    }

    public StopType getStopType() {
        return stopType;
    }

    public Site getSite() {
        return site;
    }

    public DeliveryDocument getDocument() {
        return document;
    }

    public void setStopOrder(int stopOrder) {
        validateStopOrder(stopOrder);
        this.stopOrder = stopOrder;
    }

    public void setSite(Site site) {
        if (site == null) {
            throw new IllegalArgumentException("site cannot be null");
        }
        this.site = site;
    }

    public void setDocument(DeliveryDocument document) {
        this.document = document;
    }

    public boolean hasDocument() {
        return document != null;
    }

    public boolean belongsToZone(ShippingZone shippingZone) {
        if (shippingZone == null) {
            throw new IllegalArgumentException("shippingZone cannot be null");
        }
        return site.belongsToZone(shippingZone);
    }

    private void validateStopOrder(int stopOrder) {
        if (stopOrder < 0) {
            throw new IllegalArgumentException("stopOrder cannot be negative");
        }
    }

    @Override
    public String toString() {
        return "DeliveryStop{" +
                "stopOrder=" + stopOrder +
                ", stopType=" + stopType +
                ", site=" + site.getSiteName() +
                ", hasDocument=" + (document != null) +
                '}';
    }
}