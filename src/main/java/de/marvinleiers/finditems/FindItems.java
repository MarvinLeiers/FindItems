package de.marvinleiers.finditems;

import de.marvinleiers.finditems.commands.Items;
import de.marvinleiers.finditems.listeners.ItemObtainListener;
import de.marvinleiers.finditems.listeners.PlayerJoinListener;
import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.finditems.utils.MySQL;
import de.marvinleiers.mpluginapi.mpluginapi.MPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

//TODO: Belohnung, wenn *alle* Items gesammelt
//TODO: Reihenfolge offline speichern.

public final class FindItems extends MPlugin
{
    private static ItemManager itemManager;
    private static Economy econ;
    private static MySQL mySQL;

    // Wird beim Start des Servers aufgerufen.
    @Override
    protected void onStart()
    {
        if (!setupEconomy())
        {
            log("§4Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Erstelle config.yml
        saveDefaultConfig();

        // Erstelle items.yml, rewards.yml & levels.yml
        saveResource("items.yml", false);
        saveResource("levels.yml", false);
        saveResource("rewards.yml", false);

        this.prepareMySQL();
        itemManager = ItemManager.getInstance();

        // Lade Items
        itemManager.loadItems();

        // Befehle registieren
        new Items(this, "items");

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new ItemObtainListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
    }

    // Wird beim Stopp des Servers aufgerufen.
    @Override
    protected void onStop()
    {
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
