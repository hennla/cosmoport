package com.space.model;

public enum ShipType {
    TRANSPORT("TRANSPORT"),
    MILITARY("MILITARY"),
    MERCHANT("MERCHANT");

    private String ShipType;

    ShipType(String ShipType) {
        this.ShipType = ShipType;
    }

    public String getShipType() {
        return ShipType;
    }
}