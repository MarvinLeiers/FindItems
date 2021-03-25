package de.marvinleiers.finditems.listeners;

import de.marvinleiers.finditems.utils.ItemManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ItemObtainListener implements Listener
{
    private ItemManager itemManager = ItemManager.getInstance();

    @EventHandler
    public void onPickUpItem(EntityPickupItemEvent event)
    {
        ItemStack item = event.getItem().getItemStack();

        if (!itemManager.isAllowed(item)) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        itemManager.found(item, player);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();

        if (item == null) return;
        if (!itemManager.isAllowed(item)) return;

        itemManager.found(item, player);
    }
}
