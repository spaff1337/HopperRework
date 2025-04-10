package me.spaff.hopperrework;

import org.bukkit.Color;

public class Constants {
    public static final String HOPPER_DATA_NAMESPACE = "hpr-plugin";

    // Filtering
    public static final String HOPPER_DATA_FILTER_KEY = "filter_mode";
    public static final String HOPPER_DATA_FILTER_ITEMS_KEY = "filter_items";
    public static final int MAX_FILTER_CAPACITY = 7;

    // Upgrades
    public static final String HOPPER_DATA_UPGRADES_KEY = "upgrades";
    public static final int UPGRADES_LIMIT = 3;

    // Linking
    public static final String HOPPER_DATA_LINKED_CONTAINER_KEY = "linked_container";
    public static final int NORMAL_LINK_MAX_DISTANCE = 25; // 25 Blocks
    public static final int EXTENDED_LINK_MAX_DISTANCE = 50; // 50 Blocks

    public static final Color LINKING_COLOR_ALLOWED = Color.fromRGB(3, 252, 140);
    public static final Color LINKING_COLOR_NOT_ALLOWED = Color.fromRGB(255, 66, 66);

    // Misc
    public static final int MINIMUM_MENU_CLOSE_DISTANCE = 7;
    public static final int MENU_CLICK_DELAY_MILLISECONDS = 250;
}
