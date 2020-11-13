import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        String message = new Scanner(System.in).nextLine();
        byte[] data = message.getBytes();
        DatagramPacket packetToSend = new DatagramPacket(data, 0, data.length, InetAddress.getByName("127.0.0.1"), 9087);
        socket.send(packetToSend);
        message.replace("/", "");
        System.out.println(message.replace("/", ""));
    }
}
