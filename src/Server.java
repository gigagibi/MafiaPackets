import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

public class Server {
    private static DatagramSocket socket;

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean gameIsOver = false;
        Scanner sc = new Scanner(System.in);
        socket = new DatagramSocket(9087);
        int amountOfPlayers, amountOfMafia;
        ArrayList<Client> players = new ArrayList<>();

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
                players.get(i).setRole("Мафия");

            }
            else if(i == policeIndex)
            {
                players.get(i).setRole("Комисса");
            }
            else
            {
                players.get(i).setRole("Гражданин");
            }
            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Вы " + players.get(i).getRole());
        }

        System.out.println("Игра началась!");
        for(Client p : players)
        {
            sendMessage(p.getAddress(), p.getPort(), "Игра началась!");
        }



        StringBuilder mafiaList = new StringBuilder(); //создание списка мафиози для их знакомства
        for(Client p : players)
        {
            mafiaList.append("|  ");
            if (p.getRole() == "Мафия")
                mafiaList.append(p.getName() + "  |  ");
        }

        System.out.println("Город засыпает, просыпается мафия!");
        for(Client p : players)
        {
            sendMessage(p.getAddress(), p.getPort(), "Город засыпает, просыпается мафия!");
            if(p.getRole() == "Мафия")
            {
                sendMessage(p.getAddress(), p.getPort(), "Знакомьтесь, мафия: " + mafiaList);
            }
        }
        Thread.sleep(5000);

        while(!gameIsOver) {
            ArrayList<String> votes = new ArrayList<>();
            System.out.println("Город просыпается, пора обсудить, кого выгнать! Чтобы проголосовать, введите /имяигрока");
            for(Client p : players)
            {
                sendMessage(p.getAddress(), p.getPort(), "Город просыпается, пора обсудить, кого выгнать! Чтобы проголосовать, введите /имяигрока");
            }
            int count = 0;
            while (count < amountOfPlayers) {
                String msg = receiveMessage();
                if (msg.contains("/"))
                {
                    votes.add(msg.replace("/", ""));
                }
                for (Client p : players) {
                    sendMessage(p.getAddress(), p.getPort(), msg);
                }
            }
            System.out.println("Голосование завершено! Выбывает игрок: " + getMaxFreqName(votes));
            for(Client p : players)
            {
                if (p.getName() == getMaxFreqName(votes))
                {
                    players.remove(p);
                    if(p.getRole() == "Мафия")
                    {
                        System.out.println(p.getName() + " был мафией!");
                        amountOfMafia--;
                        amountOfPlayers--;
                    }
                    else if(p.getRole() == "Комиссар")
                    {
                        System.out.println(p.getName() + " был комиссаром!");
                        amountOfPlayers--;
                    }
                    else
                    {
                        System.out.println(p.getName() + " был гражданином!");
                        amountOfPlayers--;
                    }
                    break;
                }
            }

            System.out.println("Город засыпает, просыпается мафия, чтобы выбрать жертву... Мафия, обсуждайте в чате и голосуйте /имяигрока");
            votes = new ArrayList<>();
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

    public static String getMaxFreqName(ArrayList<String> names)
    {
        int mFreq = 0;
        String mFreqName = null;
        for(int i = 0; i < names.size(); i++)
        {
            int freq = Collections.frequency(names, names.get(i));
            if (freq > mFreq)
            {
                mFreq = freq;
                mFreqName = names.get(i);
            }
        }
        return mFreqName;
    }
}
