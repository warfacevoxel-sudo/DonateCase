package com.jodexindustries.dcphysicalkey.tools;

import com.jodexindustries.dcphysicalkey.bootstrap.MainAddon;
import com.jodexindustries.donatecase.api.tools.DCTools;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jodexindustries.dcphysicalkey.bootstrap.MainAddon.NAMESPACED_KEY;
import static com.jodexindustries.donatecase.api.tools.DCTools.rc;

public class ItemManager {

    private final MainAddon addon;

    public static final Map<String, ItemStack> items = new HashMap<>();

    public ItemManager(MainAddon addon) {
        this.addon = addon;
    }

    public void load() {
        items.clear();
        ConfigurationSection section = addon.getConfig().get().getConfigurationSection("keys");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection keySection = section.getConfigurationSection(key);
            if (keySection == null) return;

            String materialId = keySection.getString("material", "STONE");

            // --- ПАТЧ: сначала пробуем ванильный Material, если не нашли —
            // пробуем через общий MaterialManager ядра (NEXO:, HEAD:, IA:, ORAXEN: и т.д.) ---
            ItemStack itemStack;
            Material material = Material.getMaterial(materialId);

            if (material != null) {
                itemStack = new ItemStack(material);
            } else {
                Object resolved = DCTools.getItemFromManager(materialId);
                if (resolved instanceof ItemStack) {
                    itemStack = ((ItemStack) resolved).clone();
                } else {
                    addon.getLogger().warning("Key " + key + ": Material not found. Skipping this key.");
                    continue;
                }
            }
            // --- конец патча ---

            String caseType = keySection.getString("case-type", "");
            if (caseType.isEmpty()) {
                addon.getLogger().warning("Key " + key + ": Case type is empty. Skipping this key.");
                continue;
            }

            if (!MainAddon.api.getCaseManager().hasByType(caseType)) {
                addon.getLogger().warning("Key " + key + ": Case type \"" + caseType + "\" not found. Skipping this key.");
                continue;
            }

            String displayName = keySection.getString("display-name", "");
            List<String> lore = keySection.getStringList("lore");

            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) {
                addon.getLogger().warning("Key " + key + ": Item meta is null. Skipping this key.");
                continue;
            }

            meta.setDisplayName(rc(displayName));
            meta.setLore(rc(lore));
            meta.getPersistentDataContainer().set(NAMESPACED_KEY, PersistentDataType.STRING, caseType);
            itemStack.setItemMeta(meta);

            items.put(key, itemStack);
        }
    }

}
