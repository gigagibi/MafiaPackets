import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class TestServer {

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(9087);
        byte[] msg = new byte[2048];
        DatagramPacket packetFromNewPlayer = new DatagramPacket(msg, 0, msg.length);
        socket.receive(packetFromNewPlayer);
        System.out.println(new String(packetFromNewPlayer.getData()));
    }

}

