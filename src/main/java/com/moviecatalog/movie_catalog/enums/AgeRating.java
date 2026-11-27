package com.moviecatalog.movie_catalog.enums;

/** Common age ratings from the US MPAA and UK BBFC systems. */
public enum AgeRating {
    G("G"),
    PG("PG"),
    PG_13("PG-13"),
    R("R"),
    NC_17("NC-17"),
    U("U"),
    TWELVE_A("12A"),
    FIFTEEN("15"),
    EIGHTEEN("18");

    private final String value;

    AgeRating(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
