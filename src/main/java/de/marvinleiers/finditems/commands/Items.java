package de.marvinleiers.finditems.commands;

import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.mpluginapi.mpluginapi.commands.Command;
import de.marvinleiers.mpluginapi.mpluginapi.utils.ItemUtils;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Items extends Command
{
    public Items(JavaPlugin plugin, String name)
    {
        super(plugin, name);
    }

    @Override
    public void onPlayerExecute(Player player, String[] strings)
    {
        ItemManager itemManager = ItemManager.getInstance();

        int y = 0, x = 0;

        for (String str : itemManager.getItemsPerLevel().get(itemManager.getPlayerLevel(player)))
        {
            Material material = Material.valueOf(str);
            Location location = player.getLocation().clone().add(x, y + 1, 0);
            spawnStand(player, createStand(location, (itemManager.has(player, material) ? "§a" : "§4§l")
                    + ItemUtils.beautifyName(material.name())));

            x += 2;

            if (x % 30 == 0)
            {
                y++;
                x = 0;
            }
        }
    }

    @Override
    public void onExecute(CommandSender commandSender, String[] strings)
    {

    }

    private void spawnStand(Player player, EntityArmorStand stand)
    {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        con.sendPacket(new PacketPlayOutSpawnEntity(stand, 78));
        con.sendPacket(new PacketPlayOutEntityMetadata(stand.getId(), stand.getDataWatcher(), false));
    }

    private EntityArmorStand createStand(Location pos, String name)
    {
        EntityArmorStand result = new EntityArmorStand(((CraftWorld) pos.getWorld()).getHandle(), pos.getX(), pos.getY(), pos.getZ());
        result.setInvisible(true);
        result.setArms(true);
        result.setMarker(true);
        result.setNoGravity(true);
        result.setBasePlate(true);

        if (!name.isEmpty())
        {
            result.setCustomName(new ChatMessage(name));
            result.setCustomNameVisible(true);
        }

        return result;
    }
}
