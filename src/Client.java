import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Client {
    private int port;
    private InetAddress address;
    private String name, message;
    private String role;
    private DatagramSocket socket;

    public void run() throws IOException{
        socket = new DatagramSocket();
        Scanner sc = new Scanner(System.in);
        System.out.println("Введите ваше имя");
        name = sc.nextLine();
        sendMessage(socket,  "127.0.0.1", 9087, name);

        String data = receiveMessage(socket); //либо вы мафия, либо житель (комиссар)
        System.out.println(data);

        data = receiveMessage(socket);//Игра началась!
        System.out.println(data);

        data = receiveMessage(socket);//Город засыпает, просыпается мафия!
        System.out.println(data);


        AtomicReference<String> msg = new AtomicReference<String>();
        msg.set("notnull");
        message = "уаыуа";

        data = receiveMessage(socket);//Жители спят, мафия знакомится (возвращается список мафиози)
        System.out.println(data);
        while(data != "Игра окончена! Победили жители" || data!= "Игра окончена! Победила мафия")
        {
            Thread write = new Thread(new Runnable()
            {
                @Override
                public void run() {
                    while(true)
                    {
                        msg.set(sc.nextLine());
                        try {
                            sendMessage(socket, "127.0.0.1", 9087, "|"+name+"|"+msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            Thread output = new Thread(new Runnable()
            {
                @Override
                public void run() {
                    while(!message.contains("Голосование завершено!"))
                    {
                        try {
                            message = receiveMessage(socket);
                            System.out.println(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            data = receiveMessage(socket); //Город просыпается, обсуждается, кто мафия
            System.out.println(data);
            write.start();
            output.start(); //начинается общение

            data = receiveMessage(socket);//сообщение о роли выбывшего
            System.out.println(data);

            data = receiveMessage(socket); //Город засыпает, мафия выбирает жертву
            System.out.println(data);

            if(data.contains("Мафия, обсуждайте в чате и голосуйте /имяигрока"))
            {
                output.start();
            }

            data = receiveMessage(socket);//смерть жертвы
            System.out.println(data);

            data = receiveMessage(socket);//комиссар в действии
            System.out.println(data);

            if(data.contains("Комиссар, введите имя игрока"))
            {
                output.start();
                /*if(message.contains("Голосование завершено!"))
                {
                    write.interrupt();
                    output.interrupt();
                }*/
            }

            data = receiveMessage(socket);//игра окончена или фаза утра
            System.out.println(data);
        }
    }


    public static void main(String[] args) throws IOException{
        new Client().run();
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

    public static void sendMessage(DatagramSocket socket, String address, int port, String message) throws IOException
    {
        
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, InetAddress.getByName(address), port);
        socket.send(packet);
    }

    public static String receiveMessage(DatagramSocket socket) throws IOException
    {
        byte[] data = new byte[2048];
        DatagramPacket packet = new DatagramPacket(data, 0, data.length);
        socket.receive(packet);
        return new String(packet.getData());
    }
}
