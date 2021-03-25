package de.marvinleiers.finditems.utils;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

public class LastItems
{
    private static final CustomConfig rewards = new CustomConfig(FindItems.getInstance().getDataFolder().getPath() + "/rewards.yml");
    private static final List<List<Material>> reihen = ItemManager.getReihen();

    private HashMap<List<Material>, Integer> currentReihen;
    private final Player player;

    public LastItems(Player player)
    {
        this.player = player;
        this.currentReihen = new HashMap<>();
    }

    public void addMaterial(Material material)
    {
        for (List<Material> reihe : reihen)
        {
            if (reihe.isEmpty())
            {
                continue;
            }

            if (reihe.get(0).name().equals(material.name()))
            {
                currentReihen.put(reihe, 0);
            }
        }

        for (Map.Entry<List<Material>, Integer> entry : currentReihen.entrySet())
        {
            int currentItemInRow = entry.getValue();
            List<Material> currentReihe = entry.getKey();

            if (currentReihe.isEmpty())
                continue;

            if (!currentReihe.get(currentItemInRow).name().equals(material.name()))
            {
                currentReihe.clear();
            }
            else
            {
                if (currentItemInRow + 1 >= currentReihe.size())
                {
                    if (currentItemInRow + 1 == 5)
                    {
                        double amount = rewards.getDouble("small-reward.money");
                        FindItems.getEconomy().depositPlayer(player, amount);

                        player.sendMessage(FindItems.getInstance().getMessages().get("message-received-small-reward", amount + ""));
                    }
                    else if (currentItemInRow + 1== 40)
                    {
                        System.out.println("Große belohnung!");
                    }
                }

                currentReihen.put(currentReihe, ++currentItemInRow);
            }
        }
    }

    public Player getPlayer()
    {
        return player;
    }
}
