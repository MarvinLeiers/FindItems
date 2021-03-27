package de.marvinleiers.finditems.commands.subcommands;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.mpluginapi.mpluginapi.commands.Subcommand;
import de.marvinleiers.mpluginapi.mpluginapi.utils.CustomConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;

public class Reset extends Subcommand
{
    @Override
    public String getName()
    {
        return "reset";
    }

    @Override
    public String getDescription()
    {
        return "Spielstand eines Spielers zurücksetzen";
    }

    @Override
    public String getSyntax()
    {
        return "/items reset";
    }

    @Override
    public String getPermission()
    {
        return "mplugin.finditems.reset";
    }

    @Override
    public void execute(Player player, String[] args)
    {
        OfflinePlayer offlinePlayer;

        if (args.length == 1)
            offlinePlayer = player;
        else
            offlinePlayer = Bukkit.getOfflinePlayer(args[1]);

        reset(offlinePlayer);
        player.sendMessage("§7Spielstand von §e" + offlinePlayer.getName() + " §7wurde zurückgesetzt!");
    }

    private void reset(OfflinePlayer offlinePlayer)
    {
        File file = new File(FindItems.getInstance().getDataFolder().getPath()
                + "/players/" + offlinePlayer.getUniqueId().toString() + ".yml");

        if (file.exists())
            file.delete();

        FindItems.getMySQL().update("DELETE FROM player_progress WHERE uuid = '" + offlinePlayer.getUniqueId().toString() +"';");
    }
}
