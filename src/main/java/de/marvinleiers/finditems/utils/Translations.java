package de.marvinleiers.finditems.utils;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import de.marvinleiers.mpluginapi.mpluginapi.utils.ItemUtils;
import org.bukkit.Material;

public class Translations
{
    private static final CustomConfig config = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/translations.yml");

    public static String get(Material material)
    {
        if (!config.isSet(material.name()))
            return ItemUtils.beautifyName(material.name());

        return config.getString(material.name());
    }

    public static Material getMaterial(String material)
    {
        String englishName = get(material);

        System.out.println("englisch " + englishName);

        return Material.valueOf(englishName);
    }

    public static String get(String material)
    {
        if (!config.isSet(material))
            return ItemUtils.beautifyName(material);

        return config.getString(material);
    }
}
