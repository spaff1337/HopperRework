package me.spaff.hopperrework.hopper;

import me.spaff.hopperrework.Constants;
import me.spaff.spflib.chunk.ChunkData;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HopperData {
    private final Hopper hopper;

    // Filtering
    private FilterMode filterMode;
    private Set<Material> filterItems;

    // Upgrades
    private Set<HopperUpgrade> upgrades;

    // Linked container location
    private Location linkedContainer;

    // Data
    private final ChunkData filterModeData;
    private final ChunkData filterItemsData;
    private final ChunkData upgradesData;
    private final ChunkData linkedContainerData;

    public HopperData(Hopper hopper) {
        this.hopper = hopper;

        this.filterModeData = new ChunkData(hopper.getBlock(), new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_FILTER_KEY));
        this.filterItemsData = new ChunkData(hopper.getBlock(), new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_FILTER_ITEMS_KEY));
        this.upgradesData = new ChunkData(hopper.getBlock(), new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_UPGRADES_KEY));
        this.linkedContainerData = new ChunkData(hopper.getBlock(), new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_LINKED_CONTAINER_KEY));
    }

    public void load() {
        filterMode = FilterMode.valueOf(filterModeData.getData().orElse(FilterMode.WHITELIST.toString()));

        // Deserialize filter items
        filterItemsData.getData().ifPresentOrElse(data -> {
            Set<Material> materialsSet = new HashSet<>();

            for (Object mat : deserializeArray(data)) {
                if (!EnumUtils.isValidEnum(Material.class, (String) mat)) continue;
                materialsSet.add(Material.valueOf((String) mat));
            }

            filterItems = materialsSet;
        }, () -> filterItems = new HashSet<>());

        // Deserialize hopper upgrades
        upgradesData.getData().ifPresentOrElse(data -> {
            Set<HopperUpgrade> upgradesSet = new HashSet<>();

            for (Object upgrade : deserializeArray(data)) {
                if (!EnumUtils.isValidEnum(HopperUpgrade.class, (String) upgrade)) continue;
                upgradesSet.add(HopperUpgrade.valueOf((String) upgrade));
            }

            upgrades = upgradesSet;
        }, () -> upgrades = new HashSet<>());

        // Deserialize linked container location
        linkedContainerData.getData().ifPresentOrElse(data -> {
            Map<String, Object> deserializedLocation = new HashMap<>();
            String[] parameters = data.split(", ");

            for (String parameter : parameters) {
                String[] keyAndValue = parameter.split("=");

                String key = keyAndValue[0].replace("}", "").replace("{", "");
                String value = keyAndValue[1].replace("}", "").replace("{", "");

                deserializedLocation.put(key, value);
            }

            linkedContainer = Location.deserialize(deserializedLocation);
        }, () -> linkedContainer = null);
    }

    public void save() {
        // Serialize filter mode
        if (filterMode != null)
            filterModeData.saveData(filterMode);
        else
            filterModeData.clearData();

        // Serialize filter items
        if (filterItems != null)
            filterItemsData.saveData(serializeArray(filterItems.toArray()));
        else
            filterItemsData.clearData();

        // Serialize hopper upgrades
        if (upgrades != null)
            upgradesData.saveData(serializeArray(upgrades.toArray()));
        else
            upgradesData.clearData();

        // Serialize linked container location
        if (linkedContainer != null)
            linkedContainerData.saveData(linkedContainer.serialize());
        else
            linkedContainerData.clearData();
    }

    public void clear() {
        filterModeData.clearData();
        filterItemsData.clearData();
        upgradesData.clearData();
        linkedContainerData.clearData();
    }

    // Linking
    public void setLinkedContainer(Location linkedContainer) {
        this.linkedContainer = linkedContainer;
    }

    public Location getLinkedContainer() {
        return linkedContainer;
    }

    public boolean isLinked() {
        return linkedContainer != null;
    }

    // Upgrades
    public void addUpgrade(HopperUpgrade upgrade) {
        if (upgrades.size() < Constants.UPGRADES_LIMIT)
            this.upgrades.add(upgrade);
    }

    public Set<HopperUpgrade> getUpgrades() {
        return upgrades;
    }

    public boolean hasSignalAmplifierUpgrade() {
        return upgrades.contains(HopperUpgrade.SIGNAL_AMPLIFIER);
    }

    public boolean hasVacuumUpgrade() {
        return upgrades.contains(HopperUpgrade.VACUUM);
    }

    public boolean hasFasterTransferUpgrade() {
        return upgrades.contains(HopperUpgrade.FASTER_TRANSFER);
    }

    // Filter
    public void addFilterItems(Material filterItem) {
        if (filterItems.size() < Constants.MAX_FILTER_CAPACITY)
            this.filterItems.add(filterItem);
    }

    public boolean canFilterItem(ItemStack item) {
        if (item == null) return true;
        if (filterItems.isEmpty()) return true;

        Material mat = item.getType();

        if (filterMode.equals(FilterMode.WHITELIST)) {
            return filterItems.contains(mat);
        }
        else { // Blacklist
            return !filterItems.contains(mat);
        }
    }

    public void setFilterMode(FilterMode filterMode) {
        this.filterMode = filterMode;
    }

    public Set<Material> getFilterItems() {
        return filterItems;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    // Serialization
    private Object serializeArray(Object[] array) {
        String serializedArray = "";
        for (Object object : array) {
            serializedArray = serializedArray.isEmpty() ? (String.valueOf(object)) : (serializedArray + ", " + object);
        }
        return serializedArray;
    }

    private Object[] deserializeArray(String serialized) {
        return serialized.split(", ");
    }
}
