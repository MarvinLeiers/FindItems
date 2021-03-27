package de.marvinleiers.finditems.listeners;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.finditems.utils.MySQL;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener
{
    private static final ItemManager itemManager = ItemManager.getInstance();

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        MySQL mySQL = FindItems.getMySQL();
        Player player = event.getPlayer();

        if (!mySQL.exists(player, "player_progress"))
        {
            mySQL.update("INSERT INTO player_progress (uuid, level_one_progress, level_two_progress, level_three_progress," +
                    "level_four_progress, level_five_progress, level) VALUES ('" + player.getUniqueId().toString() + "', 0, 0, 0, 0, 0, 1);");
        }

        System.out.println("progress " + itemManager.getPlayerLevelProgress(player));
    }
}
