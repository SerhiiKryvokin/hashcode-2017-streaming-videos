public class VideoServerPair {
    int video;
    int server;

    public VideoServerPair(int video, int server) {
        this.video = video;
        this.server = server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VideoServerPair that = (VideoServerPair) o;

        if (video != that.video) return false;
        return server == that.server;

    }

    @Override
    public int hashCode() {
        int result = video;
        result = 31 * result + server;
        return result;
    }
}
