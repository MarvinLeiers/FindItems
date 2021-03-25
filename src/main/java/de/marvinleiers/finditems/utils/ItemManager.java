package de.marvinleiers.finditems.utils;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import de.marvinleiers.mpluginapi.mpluginapi.utils.ItemFactory;
import de.marvinleiers.mpluginapi.mpluginapi.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

// Hier wird alles, das mit den Items zu tun hat, verwaltet.
public class ItemManager
{
    private static final HashMap<Player, LastItems> lastItems = new HashMap<>();
    private static final HashMap<Player, BossBar> bossBars = new HashMap<>();
    private static final HashMap<Integer, List<String>> itemsPerLevel = new HashMap<>();
    private static final List<List<Material>> reihen = new ArrayList<>();
    private static MySQL mySQL;
    private static ItemManager instance;
    private Set<Material> allowedItems;

    private ItemManager()
    {
        this.allowedItems = new HashSet<>();
        mySQL = FindItems.getMySQL();

        CustomConfig levelsConfig = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/levels.yml");

        for (int i = 1; i <= 5; i++)
        {
            List<String> items = levelsConfig.getConfig().getStringList("levels." + i);
            itemsPerLevel.put(i, items);
        }

        CustomConfig config = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/config.yml");
        List<String> reihenList = config.getConfig().getStringList("reihen");

        for (String str : reihenList)
        {
            String[] mats = str.split(", ");
            List<Material> materialList = new ArrayList<>();

            for (String material : mats)
                materialList.add(Material.valueOf(material.toUpperCase().trim()));

            reihen.add(materialList);

            System.out.println("Added list with " + materialList.size() + " items");
        }
    }

    /**
     * @param player
     * @param material
     * @return Ob der Spiel das Item bereits gefunden hat.
     */
    public boolean has(Player player, Material material)
    {
        CustomConfig customConfig = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/players/"
                + player.getUniqueId().toString() + ".yml");
        List<String> foundMaterials = customConfig.getConfig().getStringList("items");

        return foundMaterials.contains(material.name());
    }

    public boolean isValidItemForPlayer(OfflinePlayer player, ItemStack item)
    {
        return isValidItem(item) && itemsPerLevel.get(getPlayerLevel(player)).contains(item.getType().name());
    }

    private boolean isValidItem(ItemStack item)
    {
        return !ItemUtils.hasPersistentDataKey(FindItems.getInstance(), item, "mplugin.used");
    }

    public double getPlayerLevelProgress(OfflinePlayer player)
    {
        final String[] arr = {"one", "two", "three", "four", "five"};
        int playerLevel = getPlayerLevel(player);
        String columnName = "level_" + arr[playerLevel - 1] + "_progress";

        String query = "SELECT " + columnName + " FROM player_progress WHERE uuid = '" + player.getUniqueId().toString() + "';";

        try
        {
            Connection connection = mySQL.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                double progress = resultSet.getDouble(columnName);

                resultSet.close();
                preparedStatement.close();
                connection.close();

                return progress;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * @param player
     * @return Level des Spielers (per MySQL)
     */
    public int getPlayerLevel(OfflinePlayer player)
    {
        if (!mySQL.exists(player, "player_progress"))
            return 1;

        int level = 1;

        String query = "SELECT level FROM player_progress WHERE uuid='" + player.getUniqueId().toString() + "';";

        try
        {
            Connection connection = mySQL.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next())
            {
                level = resultSet.getInt("level");

                resultSet.close();
                preparedStatement.close();
                connection.close();
                return level;
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return level;
    }

    /**
     * Wird ausgeführt, wenn ein Spieler ein Item findet.
     *
     * @param itemStack
     * @param player
     */
    public void found(ItemStack itemStack, Player player)
    {
        Material material = itemStack.getType();

        // Methode soll nur Items behandeln, die zulässig sind.
        if (!isAllowed(material)) return;

        // Wenn der Spieler dieses Item bereits gefunden hat, wird das Item nicht markiert.
        if (!has(player, material) && isValidItemForPlayer(player, itemStack))
        {
            if (!lastItems.containsKey(player))
            {
                lastItems.put(player, new LastItems(player));
            }

            LastItems lastItemsFromPlayer = lastItems.get(player);
            lastItemsFromPlayer.addMaterial(material);

            new ItemFactory(FindItems.getInstance(), itemStack).addPersistentDataEntry("mplugin.used", "true");

            CustomConfig customConfig = new CustomConfig(FindItems.getInstance().getDataFolder().getPath()
                    + "/players/" + player.getUniqueId().toString() + ".yml");

            List<String> foundMaterials = customConfig.getConfig().getStringList("items");
            final String[] arr = {"one", "two", "three", "four", "five"};
            int playerLevel = getPlayerLevel(player);
            String columnName = "level_" + arr[playerLevel - 1] + "_progress";

            foundMaterials.add(material.name());
            customConfig.set("items", foundMaterials);

            FindItems.getInstance().log(player.getName() + " hat " + material.name() + " gefunden.");

            double newProgress = ((double) 1 / itemsPerLevel.get(getPlayerLevel(player)).size()) + getPlayerLevelProgress(player);
            int currentPlayerLevel = getPlayerLevel(player);

            if (newProgress >= 1)
            {
                newProgress = 1;

                giveReward(player, currentPlayerLevel);

                if (currentPlayerLevel < 5)
                {
                    setPlayerLevel(player, currentPlayerLevel + 1);

                    player.sendMessage(FindItems.getInstance().getMessages().get("message-level-up", getPlayerLevel(player) + ""));
                }
            }

            BossBar bossbar;

            if (bossBars.containsKey(player))
            {
                bossbar = bossBars.get(player);
                bossbar.setProgress(newProgress);
            }
            else
            {
                bossbar = Bukkit.createBossBar("§e§lLevel " + getPlayerLevel(player), BarColor.BLUE, BarStyle.SEGMENTED_20);
                bossbar.setProgress(newProgress);
                bossbar.addPlayer(player);
                bossBars.put(player, bossbar);
            }

            Bukkit.getScheduler().runTaskLater(FindItems.getInstance(), () -> {
                bossbar.removeAll();
                bossBars.remove(player);
            }, 20 * 5);

            Bukkit.getScheduler().runTaskLater(FindItems.getInstance(),
                    () -> player.sendMessage(FindItems.getInstance().getMessages().get("message-item-found",
                            ItemUtils.beautifyName(material.name()))), 1);

            mySQL.update("UPDATE player_progress SET " + columnName + " = " + newProgress + " WHERE uuid = '" + player.getUniqueId().toString() + "';");
        }
    }

    private void giveReward(Player player, int level)
    {
        CustomConfig rewardsConfig = new CustomConfig(FindItems.getInstance().getDataFolder().getPath()
                + "/rewards.yml");

        double moneyReward = rewardsConfig.getDouble("level-" + level + "-reward.money");
        Material material = Material.valueOf(rewardsConfig.getString("level-" + level + "-reward.item"));

        player.getInventory().addItem(new ItemStack(material));

        if (moneyReward > 0)
        {
            FindItems.getEconomy().depositPlayer(player, moneyReward);
            player.sendMessage(FindItems.getInstance().getMessages()
                    .get("message-received-small-reward", moneyReward + ""));
        }
    }

    /**
     * Level des Spielers setzen
     *
     * @param player
     * @param level
     */
    private void setPlayerLevel(Player player, int level)
    {
        mySQL.update("UPDATE player_progress SET level = " + level + " WHERE uuid ='" + player.getUniqueId().toString() + "';");
    }

    /**
     * @param item
     * @return Ob Item erlaubt ist.
     */
    public boolean isAllowed(ItemStack item)
    {
        return isAllowed(item.getType());
    }

    /**
     * @param material
     * @return Ob Item erlaubt ist
     */
    public boolean isAllowed(Material material)
    {
        return allowedItems.contains(material);
    }

    /**
     * Items aus der items.yml laden, weil so der Zugriff während der Operationszeiten schneller ist, als jedes Mal auf
     * die Datei zuzugreifen.
     */
    public void loadItems()
    {
        CustomConfig config = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/items.yml");

        for (String str : config.getConfig().getStringList("items"))
        {
            Material material;

            try
            {
                material = Material.valueOf(str);
            }
            catch (Exception e)
            {
                FindItems.getInstance().log("§c" + str + " §7ist kein zulässiges Item");
                continue;
            }

            allowedItems.add(material);
        }
    }

    public HashMap<Integer, List<String>> getItemsPerLevel()
    {
        return itemsPerLevel;
    }

    public Set<Material> getAllowedItems()
    {
        return allowedItems;
    }

    public static List<List<Material>> getReihen()
    {
        return reihen;
    }

    public static ItemManager getInstance()
    {
        if (instance == null)
            instance = new ItemManager();

        return instance;
    }
}
