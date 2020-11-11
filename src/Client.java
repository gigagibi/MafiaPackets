import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    private static int port;
    private static InetAddress address;
    private static String name;
    private static String role;
    private static DatagramSocket socket;

    public static void main(String[] args) throws IOException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите ваше имя");
        name = sc.nextLine();
        sendMessage("127.0.0.1", 9087, name);
        String data = receiveMessage();
        while(data != "Игра окончена!")
        {
            System.out.println(data);
            //if(data == "")
        }
    }

    public Client(int port, InetAddress address, String name) {
        this.port = port;
        this.address = address;
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Client() {
    }

    public static void sendMessage(String address, int port, String message) throws IOException
    {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName(address), port);
        socket.send(packet);
    }

    public static String receiveMessage() throws IOException
    {
        byte[] data = new byte[2048];
        DatagramPacket packet = new DatagramPacket(data, 0, data.length);
        socket.receive(packet);
        return new String(packet.getData());
    }
}
