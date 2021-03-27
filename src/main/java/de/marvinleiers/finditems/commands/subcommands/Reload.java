package de.marvinleiers.finditems.commands.subcommands;

import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.mpluginapi.mpluginapi.commands.Subcommand;
import org.bukkit.entity.Player;

public class Reload extends Subcommand
{
    @Override
    public String getName()
    {
        return "reload";
    }

    @Override
    public String getDescription()
    {
        return "Lade items.yml neu";
    }

    @Override
    public String getSyntax()
    {
        return "/items reload";
    }

    @Override
    public String getPermission()
    {
        return "mplugin.finditems.reload";
    }

    @Override
    public void execute(Player player, String[] strings)
    {
        ItemManager.getInstance().loadItems();

        player.sendMessage("§7Konfigurationen wurden erfolgreich neugeladen!");
    }
}
