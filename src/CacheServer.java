import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CacheServer {
    int id;
    int remainingCapacity;
    Set<Integer> videos = new HashSet<>();

    public CacheServer(int id, int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
        this.id = id;
    }
}
