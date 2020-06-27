package IO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IOServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Сервер запустился!");
            Socket socket = serverSocket.accept();  //ожидание блок операция
            System.out.println("Клиент подключился!");
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            while (true) {
                String msg = in.readUTF(); // ожидание, блок операция
                System.out.println("Сообщение клиента " + msg);
                out.writeUTF("от сервера " + msg);
            }
        } catch (IOException e) {
            System.out.println("что-то пошло не так");
            e.printStackTrace();
        }
    }
}
