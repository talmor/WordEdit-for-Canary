// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.ContainerBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.regions.Region;

/**
 * World for hMod.
 * 
 * @author sk89q
 */
public class CanaryWorld extends LocalWorld {
    private World world;
    /**
     * Logger.
     */
    private final Logger logger = Logger.getLogger("Minecraft.WorldEdit");

    /**
     * Construct the object.
     * 
     * @param world
     */
    public CanaryWorld(World world) {
        this.world = world;
    }

    /**
     * Get the world handle.
     * 
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     * Set block type.
     * 
     * @param pt
     * @param type
     * @return
     */
    public boolean setBlockType(Vector pt, int type) {
        // Can't set colored cloth or crash
        /*
         * if ((type >= 21 && type <= 34) || type == 36) { return false; }
         */
        return world.setBlockAt(type, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Get block type.
     * 
     * @param pt
     * @return
     */
    public int getBlockType(Vector pt) {
        return world.getBlockIdAt(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Set block data.
     * 
     * @param pt
     * @param data
     * @return
     */
    public void setBlockData(Vector pt, int data) {
        world.setBlockData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data);
    }

    /**
     * Get block data.
     * 
     * @param pt
     * @return
     */
    public int getBlockData(Vector pt) {
        return world.getBlockData(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Set sign text.
     * 
     * @param pt
     * @param text
     */
    public void setSignText(Vector pt, String[] text) {
        Sign signData = (Sign) world.getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return;
        }
        for (byte i = 0; i < 4; i++) {
            signData.setText(i, text[i]);
        }
        signData.update();
    }

    /**
     * Get sign text.
     * 
     * @param pt
     * @return
     */
    public String[] getSignText(Vector pt) {
        Sign signData = (Sign) world.getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return new String[] { "", "", "", "" };
        }
        String[] text = new String[4];
        for (byte i = 0; i < 4; i++) {
            text[i] = signData.getText(i);
        }
        return text;
    }

    /**
     * Gets the contents of chests. Will return null if the chest does not
     * really exist or it is the second block for a double chest.
     * 
     * @param pt
     * @return
     */
    public BaseItemStack[] getChestContents(Vector pt) {
        ComplexBlock cblock = world.getOnlyComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        BaseItemStack[] items;
        Item[] nativeItems;

        if (cblock instanceof Chest) {
            Chest chest = (Chest) cblock;
            nativeItems = chest.getContents();
        } else {
            return null;
        }

        items = new BaseItemStack[nativeItems.length];

        for (byte i = 0; i < nativeItems.length; i++) {
            Item item = nativeItems[i];

            if (item != null) {
                items[i] = new BaseItemStack((short) item.getItemId(), item.getAmount(), (short) item.getDamage());
            }
        }

        return items;
    }

    /**
     * Sets a chest slot.
     * 
     * @param pt
     * @param contents
     * @return
     */
    public boolean setChestContents(Vector pt, BaseItemStack[] contents) {

        ComplexBlock cblock = world.getOnlyComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (cblock instanceof Chest) {
            Chest chest = (Chest) cblock;
            Item[] nativeItems = new Item[contents.length];

            for (int i = 0; i < contents.length; i++) {
                BaseItemStack item = contents[i];

                if (item != null) {

                    Item nativeItem = new Item(item.getType(), item.getAmount());
                    nativeItem.setDamage(item.getDamage());
                    nativeItems[i] = nativeItem;
                }
            }

            setContents(chest, nativeItems);
        }

        return false;
    }

    /**
     * Clear a chest's contents.
     * 
     * @param pt
     */
    public boolean clearChest(Vector pt) {
        ComplexBlock cblock = world.getOnlyComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (cblock instanceof Chest) {
            Chest chest = (Chest) cblock;
            chest.clearContents();
            chest.update();
            return true;
        }

        return false;
    }

    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    private void setContents(ItemArray<?> itemArray, Item[] contents) {
        int size = contents.length;

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i].getItemId(), contents[i].getAmount(), contents[i].getDamage(), i);
            }
        }
    }

    /**
     * Set mob spawner mob type.
     * 
     * @param pt
     * @param mobType
     */
    public void setMobSpawnerType(Vector pt, String mobType) {
        ComplexBlock cblock = getWorld().getComplexBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof MobSpawner)) {
            return;
        }

        MobSpawner mobSpawner = (MobSpawner) cblock;
        mobSpawner.setSpawn(mobType);
        mobSpawner.update();
    }

    /**
     * Get mob spawner mob type. May return an empty string.
     * 
     * @param pt
     * @param mobType
     */

    /**
     * Generate a tree at a location.
     * 
     * @param pt
     * @return
     */
    public boolean generateTree(EditSession editSession, Vector pt) {
        try {
            return MinecraftServerInterface.generateTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to create tree (do you need to update WorldEdit "
                    + "due to a Minecraft update?)", t);
            return false;
        }
    }

    /**
     * Generate a big tree at a location.
     * 
     * @param pt
     * @return
     */
    public boolean generateBigTree(EditSession editSession, Vector pt) {
        try {
            return MinecraftServerInterface.generateBigTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to create big tree (do you need to update WorldEdit "
                    + "due to a Minecraft update?)", t);
            return false;
        }
    }

    /**
     * Drop an item.
     * 
     * @param pt
     * @param type
     * @param count
     * @param times
     */
    public void dropItem(Vector pt, int type, int count) {
        etc.getServer().getWorld(1).dropItem(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), type, count);
    }

    /**
     * Kill mobs in an area.
     * 
     * @param origin
     * @param radius
     * @return
     */
    public int killMobs(Vector origin, int radius) {
        int killed = 0;

        for (Mob mob : etc.getServer().getWorld(1).getMobList()) {
            Vector mobPos = new Vector(mob.getX(), mob.getY(), mob.getZ());
            if (mob.getHealth() > 0 && (radius == -1 || mobPos.distance(origin) <= radius)) {
                mob.setHealth(0);
                killed++;
            }
        }

        for (Mob mob : etc.getServer().getWorld(1).getAnimalList()) {
            Vector mobPos = new Vector(mob.getX(), mob.getY(), mob.getZ());
            if (mob.getHealth() > 0 && (radius == -1 || mobPos.distance(origin) <= radius)) {
                mob.setHealth(0);
                killed++;
            }
        }

        return killed;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CanaryWorld) {
            return ((CanaryWorld) other).getWorld().equals(world);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return world.getType().getId();
    }

    @Override
    public int getBlockLightLevel(Vector pt) {
        // TODO Implement
        return 0;
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return false;
    }

    @Override
    public boolean copyToWorld(Vector pt, BaseBlock block) {
        return false;
    }

    @Override
    public boolean copyFromWorld(Vector pt, BaseBlock block) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean clearContainerBlockContents(Vector pt) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        try {
            return MinecraftServerInterface.generateBirchTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to create birch tree (do you need to update WorldEdit "
                    + "due to a Minecraft update?)", t);
            return false;
        }
    }

    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        try {
            return MinecraftServerInterface.generateRedwoodTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to create Redwood tree (do you need to update WorldEdit "
                    + "due to a Minecraft update?)", t);
            return false;
        }
    }

    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        try {
            return MinecraftServerInterface.generateTallRedwoodTree(editSession, pt);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Failed to create tall redwood tree (do you need to update WorldEdit "
                    + "due to a Minecraft update?)", t);
            return false;
        }
    }

    @Override
    public void dropItem(Vector pt, BaseItemStack item) {
        world.dropItem(CanaryUtil.toLocation(world, pt), item.getType(), item.getAmount());
    }

    @Override
    public int killMobs(Vector origin, int radius, boolean killPets) {
        int num = 0;
        double radiusSq = Math.pow(radius, 2);
        for (LivingEntity ent : world.getLivingEntityList()) {
            OEntity oent = ent.getEntity();
            if (!killPets && oent instanceof OEntityWolf && ((OEntityWolf) oent).A()) {
                continue; // tamed wolf
            }
            if (oent instanceof OEntityCreature || oent instanceof OEntityGhast || oent instanceof OEntitySlime) {
                WorldVector vector = new WorldVector(this, ent.getX(), ent.getY(), ent.getZ());
                if (radius == -1 || origin.distanceSq(vector) <= radiusSq) {
                    ent.setHealth(0);
                    num++;
                }
            }
        }

        return num;
    }

    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        int num = 0;
        double radiusSq = Math.pow(radius, 2);
        for (BaseEntity ent : world.getEntityList()) {
            WorldVector vector = new WorldVector(this, ent.getX(), ent.getY(), ent.getZ());
            if (radius != -1 && origin.distanceSq(vector) > radiusSq) {
                continue;
            }
            if (type == EntityType.ARROWS) {
                if (ent.entity instanceof OEntityArrow) {
                    ent.entity.bh = true;
                    num++;
                }
            } else if (type == EntityType.BOATS) {
                if (ent.entity instanceof OEntityBoat) {
                    ent.entity.bh = true;
                    num++;
                }
            } else if (type == EntityType.ITEMS) {
                if (ent.entity instanceof OEntityItem) {
                    ent.entity.bh = true;
                    num++;
                }
            } else if (type == EntityType.MINECARTS) {
                if (ent.entity instanceof OEntityMinecart) {
                    ent.entity.bh = true;
                    num++;
                }
            } else if (type == EntityType.PAINTINGS) {
                if (ent.entity instanceof OEntityPainting) {
                    ent.entity.bh = true;
                    num++;
                }
            } else if (type == EntityType.TNT) {
                if (ent.entity instanceof OEntityTNTPrimed) {
                    ent.entity.bh = true;
                    num++;
                }
            }
        }
        return num;
    }
}
