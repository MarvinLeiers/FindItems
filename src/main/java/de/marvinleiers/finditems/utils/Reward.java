package de.marvinleiers.finditems.utils;

import org.bukkit.Material;

public class Reward
{
    private final Material material;
    private final double money;

    public Reward(Material material, double money)
    {
        this.material = material;
        this.money = money;
    }

    public Material getMaterial()
    {
        return material;
    }

    public double getMoney()
    {
        return money;
    }
}
