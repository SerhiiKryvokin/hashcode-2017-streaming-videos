import java.util.ArrayList;

public class EndPoint {
    int id;
    int defaultLatency;
    ArrayList<EndPointToServerConn> connections = new ArrayList<>();

    public EndPoint(int id, int defaultLatency) {
        this.defaultLatency = defaultLatency;
        this.id = id;
    }
}
