package de.marvinleiers.finditems.utils;

public enum Level
{
    LEVEL_ONE(1),
    LEVEL_TWO(2),
    LEVEL_THREE(3),
    LEVEL_FOUR(4),
    LEVEL_FIVE(5);

    private static final ItemManager itemManager = ItemManager.getInstance();
    private final int level;

    Level(int level)
    {
        this.level = level;
    }

    public int getLevel()
    {
        return level;
    }

    public int getItemAmount()
    {
        return itemManager.getItemsPerLevel().get(getLevel()).size();
    }
}
