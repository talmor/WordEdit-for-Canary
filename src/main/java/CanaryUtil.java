import com.sk89q.worldedit.Vector;


public class CanaryUtil {


    public static Location toLocation(World world, Vector pt) {
        return new Location(world,pt.getX(),pt.getY(),pt.getZ());
    }

}
