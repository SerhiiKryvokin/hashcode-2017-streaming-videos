import java.io.*;
import java.util.*;

public class Main {

    int nVideos;
    int nEndPoints;
    int nReqDescr;
    int nCacheServ;
    int cacheCapacity;
    int[] videosSize;
    EndPoint[] endPoints;
    CacheServer[] cacheServers;
    Request[] requests;

    int[] bestLatency;
    double[][] scores;
    HashMap<Integer, ArrayList<Integer>> videoToRequests = new HashMap<>();

    void solve() {
        //initialization
        nVideos = in.nextInt();
        nEndPoints = in.nextInt();
        nReqDescr = in.nextInt();
        nCacheServ = in.nextInt();
        cacheCapacity = in.nextInt();
        endPoints = new EndPoint[nEndPoints];
        cacheServers = new CacheServer[nCacheServ];
        requests = new Request[nReqDescr];
        videosSize = new int[nVideos];
        bestLatency = new int[nReqDescr];

        for (int i = 0; i < nCacheServ; i++) {
            cacheServers[i] = new CacheServer(i, cacheCapacity);
        }

        for (int i = 0; i < nVideos; i++) {
            videosSize[i] = in.nextInt();
        }

        for (int i = 0; i < nEndPoints; i++) {
            int latD = in.nextInt();
            endPoints[i] = new EndPoint(i, latD);
            int nCons = in.nextInt();
            for (int j = 0; j < nCons; j++) {
                int serverId = in.nextInt();
                int latency = in.nextInt();
                endPoints[i].connections.add(new EndPointToServerConn(serverId, latency));
            }
        }

        //score[video][server] is the measure of achieved improvement after posting video to server
        scores = new double[nVideos][nCacheServ];

        for (int i = 0; i < nReqDescr; i++) {
            int videoId = in.nextInt();
            int endPointId = in.nextInt();
            int num = in.nextInt();
            requests[i] = new Request(i, endPointId, videoId, num);
            bestLatency[i] = endPoints[endPointId].defaultLatency;
            if (videoToRequests.get(videoId) == null) {
                videoToRequests.put(videoId, new ArrayList<>());
            }
            videoToRequests.get(videoId).add(i);
            accumulateScores(requests[i]);
        }

        /*
        to get <video, server> pair of best score we need sorted structure
        after each posting of video to server bestLatencies for that video might be changed too
        so need to recalculate scores[posted video][j], j : 0..nCacheServ - 1
        since sorting depends on scores, need to remove all <posted video, server> pairs and insert them again after scores recalculation
        Tree allows to do both remove and insert with log asymptotic
         */
        TreeSet<VideoServerPair> sortedPairs = new TreeSet<>((e2, e1) -> {
            int scoreCmp = Double.compare(scores[e1.video][e1.server], scores[e2.video][e2.server]);
            int videoCmp = Integer.compare(e2.video, e1.video);
            int serverCmp = Integer.compare(e2.server, e1.server);
            if (scoreCmp != 0)
                return scoreCmp;
            if (videoCmp != 0)
                return videoCmp;
            return serverCmp;
        });

        for (int i = 0; i < nVideos; i++) {
            for (int j = 0; j < nCacheServ; j++) {
                if (videoToRequests.get(i) != null && scores[i][j] > 0)
                    sortedPairs.add(new VideoServerPair(i, j));
            }
        }

        while (!sortedPairs.isEmpty()) {
            VideoServerPair best = sortedPairs.first();
            sortedPairs.remove(best);
            if (cacheServers[best.server].remainingCapacity < videosSize[best.video])
                continue;
            cacheServers[best.server].videos.add(best.video);
            cacheServers[best.server].remainingCapacity -= videosSize[best.video];
            for (int i = 0; i < nCacheServ; i++) {
                sortedPairs.remove(new VideoServerPair(best.video, i));
                scores[best.video][i] = 0;
            }

            for (Integer requestId : videoToRequests.get(best.video)) {
                bestLatency[requestId] = caclBestLatency(requests[requestId]);
                accumulateScores(requests[requestId]);
            }

            for (int i = 0; i < nCacheServ; i++) {
                if (scores[best.video][i] > 0 && cacheServers[i].remainingCapacity >= videosSize[best.video]) {
                    sortedPairs.add(new VideoServerPair(best.video, i));
                }
            }
        }

        printResult();
    }

    void accumulateScores(Request request) {
        for (EndPointToServerConn conn : endPoints[request.endPoint].connections) {
            if (videosSize[request.video] <= cacheServers[conn.server].remainingCapacity) {
                scores[request.video][conn.server] +=
                        Math.max(0, bestLatency[request.id] - conn.latency) * (double) request.reqNum / videosSize[request.video];
            }
        }
    }

    int caclBestLatency(Request request) {
        int res = endPoints[request.endPoint].defaultLatency;
        for (EndPointToServerConn conn : endPoints[request.endPoint].connections) {
            if (cacheServers[conn.server].videos.contains(request.video)) {
                res = Math.min(res, conn.latency);
            }
        }
        return res;
    }

    void printResult() {
        int count = 0;
        for (int i = 0; i < nCacheServ; i++) {
            if (!cacheServers[i].videos.isEmpty())
                count++;
        }
        out.println(count);
        for (int i = 0; i < nCacheServ; i++) {
            if (!cacheServers[i].videos.isEmpty()) {
                out.print(i + " ");
                for (Integer videoId : cacheServers[i].videos) {
                    out.print(videoId + " ");
                }
            }
            out.println();
        }
    }

    Scanner in;
    PrintWriter out;

    void run() {
        try {
            in = new Scanner(new File("A.in"));
            out = new PrintWriter(new File("A.out"));

            solve();

            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void runIO() {

        in = new Scanner(System.in);
        out = new PrintWriter(System.out);

        solve();

        out.close();
    }

    class Scanner {
        BufferedReader br;
        StringTokenizer st;

        public Scanner(File f) {
            try {
                br = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        public Scanner(InputStream f) {
            br = new BufferedReader(new InputStreamReader(f));
        }

        String next() {
            while (st == null || !st.hasMoreTokens()) {
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s == null)
                    return null;
                st = new StringTokenizer(s);
            }
            return st.nextToken();
        }

        private boolean isSpaceChar(int c) {
            return !(c >= 33 && c <= 126);
        }

        private int skip() {
            int b;
            while ((b = read()) != -1 && isSpaceChar(b)) ;
            return b;
        }

        private int read() {
            int res = -1;
            try {
                res = br.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return res;
        }

        char[] nextCharArray(int size) {
            char[] buf = new char[size];
            int b = skip(), p = 0;
            while (p < size && !(isSpaceChar(b))) {
                buf[p++] = (char) b;
                b = read();
            }
            return size == p ? buf : Arrays.copyOf(buf, p);
        }

        char[][] nextCharMap(int n, int m) {
            char[][] map = new char[n][];
            for (int i = 0; i < n; i++) {
                map[i] = nextCharArray(m);
            }
            return map;
        }

        boolean hasMoreTokens() {
            while (st == null || !st.hasMoreTokens()) {
                String s = null;
                try {
                    s = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (s == null)
                    return false;
                st = new StringTokenizer(s);
            }
            return true;
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}