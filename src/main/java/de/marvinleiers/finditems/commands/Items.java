package de.marvinleiers.finditems.commands;

import de.marvinleiers.finditems.FindItems;
import de.marvinleiers.finditems.commands.subcommands.Reload;
import de.marvinleiers.finditems.commands.subcommands.Reset;
import de.marvinleiers.finditems.utils.ItemManager;
import de.marvinleiers.finditems.utils.Translations;
import de.marvinleiers.mpluginapi.mpluginapi.commands.RootCommand;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public class Items extends RootCommand
{
    private static final HashMap<Player, ArrayList<EntityArmorStand>> stands = new HashMap<Player, ArrayList<EntityArmorStand>>();
    private static final HashMap<Player, Location> positions = new HashMap<>();
    private static final ItemManager itemManager = ItemManager.getInstance();

    public Items(JavaPlugin plugin, String name)
    {
        super(plugin, name);

        addSubcommand(new Reset());
        addSubcommand(new Reload());
    }

    @Override
    protected void onNoArguments(Player player, String[] args)
    {
        if (stands.get(player) != null)
        {
            removeOld(player);
            return;
        }

        int y = 0, x = 0;

        ArrayList<EntityArmorStand> list = new ArrayList<>();

        positions.put(player, player.getLocation().clone().add(x, y + 1, 0).add(player.getLocation().getDirection()));

        double then = 0;

        for (String str : itemManager.getItemsPerLevel().get(itemManager.getPlayerLevel(player)))
        {
            Material material = Material.valueOf(str);

            double now = 0;

            if (material.name().length() >= 12)
            {
                now += 0.75;
            }

            if (material.name().length() >= 16)
            {
                now += 0.5;
            }

            Location location = player.getLocation().clone().add(x + now + then, y + 1, 0).add(player.getLocation().getDirection());
            EntityArmorStand entityArmorStand = createStand(location, (itemManager.has(player, material) ? "§a" : "§4§l")
                    + Translations.get(material));
            spawnStand(player, entityArmorStand);

            list.add(entityArmorStand);

            x += 2;

            int perRows = FindItems.getInstance().getConfig().getInt("holograms-per-row") == 0 ? 30 : FindItems.getInstance().getConfig().getInt("holograms-per-row");

            if (x >= perRows)
            {
                y++;
                x = 0;
                then = 0;
            }
            else if (material.name().length() >= 12)
                then += 0.75;
        }

        stands.put(player, list);
    }

    private static void spawnStand(Player player, EntityArmorStand stand)
    {
        PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
        con.sendPacket(new PacketPlayOutSpawnEntity(stand, 78));
        con.sendPacket(new PacketPlayOutEntityMetadata(stand.getId(), stand.getDataWatcher(), false));
    }

    private static EntityArmorStand createStand(Location pos, String name)
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

    public static void update(Player player)
    {
        removeOld(player);

        int x = 0, y = 0;

        ArrayList<EntityArmorStand> list = new ArrayList<>();

        double then = 0;

        for (String str : itemManager.getItemsPerLevel().get(itemManager.getPlayerLevel(player)))
        {
            Material material = Material.valueOf(str);

            double now = 0;

            if (material.name().length() >= 12)
            {
                now += 0.75;
            }

            Location location = positions.get(player).clone().add(x + now + then, y, 0);
            EntityArmorStand entityArmorStand = createStand(location, (itemManager.has(player, material) ? "§a" : "§4§l")
                    + Translations.get(material));
            spawnStand(player, entityArmorStand);

            list.add(entityArmorStand);

            x += 2;

            int perRows = FindItems.getInstance().getConfig().getInt("holograms-per-row") == 0 ? 30 : FindItems.getInstance().getConfig().getInt("holograms-per-row");

            if (x % perRows == 0)
            {
                y++;
                x = 0;
                then = 0;
            }

            if (material.name().length() >= 12)
                then += 0.75;
        }

        stands.put(player, list);
    }

    public static void removeOld(Player player)
    {
        if (stands.get(player) != null)
        {
            for (EntityArmorStand stand : stands.get(player))
            {
                PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(stand.getId());

                PlayerConnection con = ((CraftPlayer) player).getHandle().playerConnection;
                con.sendPacket(destroy);
            }

            stands.remove(player);
        }
    }
}
