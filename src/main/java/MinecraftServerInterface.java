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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Random;
import java.lang.reflect.*;
import sun.reflect.ReflectionFactory;
import com.sk89q.worldedit.*;

/**
 *
 * @author sk89q
 */
public class MinecraftServerInterface {
    /**
     * Logger.
     */
    private static final Logger logger = Logger.getLogger("Minecraft.WorldEdit");
    /**
     * Random generator.
     */
    private static Random random = new Random();
    /**
     * Proxy for the tree generator.
     */
    private static MinecraftSetBlockProxy proxy;

    /**
     * Perform world generation at a location.
     *
     * @param pt
     * @return
     */
    private static boolean performWorldGen(EditSession editSession, Vector pt,
    		OWorldGenerator worldGen) {
        if (proxy == null) {
            try {
                proxy = createNoConstructor(MinecraftSetBlockProxy.class);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "setBlock() proxy class failed to construct",
                        t);
                return false;
            }
        }
        proxy.setEditSession(editSession);

        OWorldGenerator gen = worldGen;
        return gen.a(proxy, random,
                pt.getBlockX(), pt.getBlockY() + 1, pt.getBlockZ());
    }

    /**
     * Generate a tree at a location.
     *
     * @param pt
     * @return
     */
    public static boolean generateTree(EditSession editSession, Vector pt) {
        return performWorldGen(editSession, pt, new OWorldGenTrees());
    }

    /**
     * Generate a big tree at a location.
     *
     * @param pt
     * @return
     */
    public static boolean generateBigTree(EditSession editSession, Vector pt) {
        return performWorldGen(editSession, pt, new OWorldGenBigTree());
    }

    

    /**
     * Instantiate a class without calling its constructor.
     *
     * @param <T>
     * @param clazz
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("rawtypes")
    private static <T> T createNoConstructor(Class<T> clazz) throws Throwable {
        try {
            ReflectionFactory factory = ReflectionFactory.getReflectionFactory();
            Constructor objectConstructor = Object.class.getDeclaredConstructor();
            Constructor c = factory.newConstructorForSerialization(
                clazz, objectConstructor
            );
            return clazz.cast(c.newInstance());
        } catch (Throwable e) {
            throw e;
        }
    }
}
