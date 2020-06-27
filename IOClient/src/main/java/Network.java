import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Network(int port) throws IOException {
        socket = new Socket("127.0.0.1", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void sendMessage(String msg) throws IOException {
        out.writeUTF(msg);
    }

    public String reedMessage() throws IOException {
        return in.readUTF();
    }

    public void close() {
        try {
            if(in!=null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if(out!=null){
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
