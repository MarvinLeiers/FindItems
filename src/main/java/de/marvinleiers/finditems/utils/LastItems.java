package de.marvinleiers.finditems.utils;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LastItems
{
    private static final HashMap<List<Material>, Reward> reihen = ItemManager.getReihen();
    private static final ItemManager itemManager = ItemManager.getInstance();

    private final List<String> currentReihen;
    private final Player player;

    public LastItems(Player player)
    {
        this.player = player;
        this.currentReihen = new ArrayList<>();
    }

    public void addMaterial(Material material)
    {
        for (Map.Entry<List<Material>, Reward> entry : reihen.entrySet())
        {
            if (hasRow(player, entry.getKey()))
            {
                Reward reward = entry.getValue();
                Material rewardItem = reward.getMaterial();
                FindItems.getEconomy().depositPlayer(player, reward.getMoney());

                if (rewardItem != null)
                {
                    if (player.getInventory().firstEmpty() == -1)
                    {
                        player.getWorld().dropItem(player.getLocation(), new ItemStack(rewardItem));
                    }
                    else
                    {
                        player.getInventory().addItem(new ItemStack(rewardItem));
                    }
                }

                player.sendMessage(FindItems.getInstance().getMessages().get("message-received-small-reward", reward.getMoney() + ""));
                player.sendMessage(FindItems.getInstance().getMessages().get("message-row-complete"));

                String str = "";

                for (Material material1 : entry.getKey())
                {
                    str += material1.name();
                }

                currentReihen.add(str);
            }
        }
    }

    private boolean hasRow(Player player, List<Material> list)
    {
        String str = "";

        for (Material material1 : list)
        {
            str += material1.name();
        }

        if (currentReihen.contains(str))
        {
            return false;
        }

        for (Material material : list)
        {
            if (!itemManager.has(player, material))
                return false;
        }

        return true;
    }

    public Player getPlayer()
    {
        return player;
    }
}
