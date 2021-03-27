package de.marvinleiers.finditems;

import de.marvinleiers.finditems.commands.Items;
import de.marvinleiers.finditems.listeners.ItemObtainListener;
import de.marvinleiers.finditems.listeners.PlayerJoinListener;
import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.finditems.utils.MySQL;
import de.marvinleiers.mpluginapi.mpluginapi.MPlugin;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Map;

//TODO: nothing

public final class FindItems extends MPlugin
{
    private static ItemManager itemManager;
    private static Economy econ;
    private static MySQL mySQL;

    // Wird beim Start des Servers aufgerufen.
    @Override
    protected void onStart()
    {
        World world = Bukkit.getWorld("world");
        world.spawnEntity(new Location(world, 1, 1, 1), EntityType.ILLUSIONER);

        if (!setupEconomy())
        {
            log("§4Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Erstelle config.yml
        saveDefaultConfig();

        // Erstelle items.yml, rewards.yml, translations.yml & levels.yml
        saveResource("items.yml", false);
        saveResource("levels.yml", false);
        saveResource("rewards.yml", false);
        saveResource("translations.yml", false);

        this.prepareMySQL();
        itemManager = ItemManager.getInstance();

        // Lade Items
        itemManager.loadItems();

        // Befehle registieren
        new Items(this, "items");

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new ItemObtainListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        // Translations
        CustomConfig config = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/translations.yml");

        for (Map.Entry<String, Object> entry : config.getSection("").getValues(false).entrySet())
        {
            config.set((String) entry.getValue(), entry.getKey());
        }
    }

    // Wird beim Stopp des Servers aufgerufen.
    @Override
    protected void onStop()
    {
        for (Player player : Bukkit.getOnlinePlayers())
            Items.removeOld(player);

        mySQL.close();
    }

    private boolean setupEconomy()
    {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
        {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null)
        {
            return false;
        }

        econ = rsp.getProvider();

        return econ != null;
    }

    private void prepareMySQL()
    {
        mySQL = new MySQL(getConfig().getString("mysql-host"), getConfig().getString("mysql-database"),
                getConfig().getString("mysql-user"), getConfig().getString("mysql-password"));

        mySQL.connect();
        mySQL.createTable("player_progress", "(id INTEGER AUTO_INCREMENT PRIMARY KEY, uuid TEXT, " +
                "level_one_progress FLOAT, level_two_progress FLOAT, level_three_progress FLOAT, " +
                "level_four_progress FLOAT, level_five_progress FLOAT, level INTEGER)");
    }

    public static Economy getEconomy()
    {
        return econ;
    }

    public static MySQL getMySQL()
    {
        return mySQL;
    }

    public static FindItems getInstance()
    {
        return getPlugin(FindItems.class);
    }
}
