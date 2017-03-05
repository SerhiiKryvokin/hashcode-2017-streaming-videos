
public class Request {
    int id;
    int endPoint;
    int video;
    int reqNum;

    public Request(int id, int endPoint, int video, int reqNum) {
        this.endPoint = endPoint;
        this.video = video;
        this.reqNum = reqNum;
        this.id = id;
    }
}
