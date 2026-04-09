package org.example.mastermind.model;

public enum ShapeType {
    CIRCLE("Kreis"),
    RECTANGLE("Rechteck"),
    TRIANGLE("Dreieck"),
    DIAMOND("Raute"),
    STAR("Stern"),
    HEXAGON("Sechseck"),
    LINE("Linie"),
    ELLIPSE("Ellipse");

    private final String displayName;

    ShapeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
