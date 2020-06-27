
import java.io.IOException;
import java.net.Socket;

public class NIOClient implements Runnable {

    private Socket socket;

    @Override
    public void run() {
        try {
            socket = new Socket("localhost", 8189);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
