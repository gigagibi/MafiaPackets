import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Server {
    private static DatagramSocket socket;

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean GameIsOver = false;
        Scanner sc = new Scanner(System.in);
        socket = new DatagramSocket(9087);
        DatagramPacket packetToSend, packetToReceive;
        int amountOfPlayers, amountOfMafia;
        ArrayList<Client> players = new ArrayList<>();
        String message; byte[] msg;

        System.out.println("Сколько игроков?: ");
        amountOfPlayers = sc.nextInt();
        int currentPlayers = 0;
        while(currentPlayers != amountOfPlayers)
        {

            System.out.println("Ожидаем игроков. Осталось игроков: " + (amountOfPlayers - currentPlayers));
            byte[] data = new byte[2048];
            DatagramPacket packetFromNewPlayer = new DatagramPacket(data, 0, data.length);
            socket.receive(packetFromNewPlayer);
            currentPlayers++;
            players.add(new Client(packetFromNewPlayer.getPort(), packetFromNewPlayer.getAddress(), new String(packetFromNewPlayer.getData())));
        }

        System.out.println("Все игроки подключены! Пора распределять роли. Сколько мафиози должно быть в игре?: ");
        amountOfMafia = sc.nextInt();
        ArrayList<Integer> mafiaIndexes = new ArrayList<>();

        for(int i = 0; i < amountOfMafia; i++)
        {
            int a = new Random().nextInt(6);
            if(!mafiaIndexes.contains(a))
                mafiaIndexes.add(a);
        }

        int policeIndex = -1;
        while(policeIndex < 0 || mafiaIndexes.contains(policeIndex))
        {
            policeIndex = new Random().nextInt(6);
        }

        for(int i = 0; i < players.size(); i++) // распределение ролей
        {
            if(mafiaIndexes.contains(i))
            {
                players.get(i).setRole("Mafia");

            }
            else if(i == policeIndex)
            {
                players.get(i).setRole("Police");
            }
            else
            {
                players.get(i).setRole("Citizen");
            }
            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Вы " + players.get(i).getRole());
        }

        System.out.println("Игра началась!");
        for(Client p : players)
        {
            sendMessage(p.getAddress(), p.getPort(), "Игра началась!");
        }

        System.out.println("Город засыпает, просыпается мафия!");

        StringBuilder mafiaList = new StringBuilder(); //создание списка мафиози для их знакомства
        for(Client p : players)
        {
            mafiaList.append("|  ");
            if (p.getRole() == "Mafia")
                mafiaList.append(p.getName() + "  |  ");
        }

        for(Client p : players)
        {
            sendMessage(p.getAddress(), p.getPort(), "Город засыпает, просыпается мафия!");
            if(p.getRole() == "Mafia")
            {
                sendMessage(p.getAddress(), p.getPort(), "Мафия: " + mafiaList);
            }
        }


    }

    public static void sendMessage(InetAddress address, int port, String message) throws IOException
    {
        byte[] data = message.getBytes();
        DatagramPacket packet = new DatagramPacket(data, 0, data.length, address, port);
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
