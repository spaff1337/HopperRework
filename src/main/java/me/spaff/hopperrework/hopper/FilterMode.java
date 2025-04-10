package me.spaff.hopperrework.hopper;

public enum FilterMode {
    WHITELIST("Whitelist", "&f"),
    BLACKLIST("Blacklist", "&8");

    private final String name;
    private final String color;

    FilterMode(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }
}
