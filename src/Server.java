import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
    private static DatagramSocket socket;
    static ArrayList<Client> players = new ArrayList<>();
    public static void main(String[] args) throws IOException {
        boolean gameIsOver = false;
        Scanner sc = new Scanner(System.in);

        socket = new DatagramSocket(9087);
        System.out.println("Сколько игроков?: ");
        int amountOfPlayers = sc.nextInt();

        int currentPlayers = 0;
        while(currentPlayers != amountOfPlayers)
        {
            System.out.println("Ожидаем игроков. Осталось игроков: " + (amountOfPlayers - currentPlayers));
            byte[] data = new byte[10];
            DatagramPacket packetFromNewPlayer = new DatagramPacket(data, 0, data.length);
            socket.receive(packetFromNewPlayer);
            String name = (new String(packetFromNewPlayer.getData()));
            String replace = name.replace("\0", "");
            players.add(new Client(packetFromNewPlayer.getPort(), packetFromNewPlayer.getAddress(), replace));
            System.out.println(players.get(currentPlayers).getName());
            currentPlayers++;
        }

        System.out.println("Все игроки подключены! Пора распределять роли. Сколько мафиози должно быть в игре?: ");
        int amountOfMafia = sc.nextInt();
        ArrayList<Integer> mafiaIndexes = new ArrayList<>();
        for(int i = 0; i < amountOfMafia; i++)
        {
            int a = new Random().nextInt(amountOfPlayers);
            if(!mafiaIndexes.contains(a))
                mafiaIndexes.add(a);
        }

        int policeIndex = -1;
        while(policeIndex < 0 || mafiaIndexes.contains(policeIndex))
        {
            policeIndex = new Random().nextInt(amountOfPlayers);
        }

        for(int i = 0; i < amountOfPlayers; i++) // распределение ролей
        {
            if(mafiaIndexes.contains(i))
            {
                players.get(i).setRole("Мафия");

            }
            else if(i == policeIndex)
            {
                players.get(i).setRole("Комиссар");
            }
            else
            {
                players.get(i).setRole("Гражданин");
            }
            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Вы " + players.get(i).getRole());
        }

        System.out.println("Игра началась!");
        for(int i = 0; i < amountOfPlayers; i++)
        {
            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Игра началась!");
        }

        StringBuilder mafiaList = new StringBuilder(); //создание списка мафиози для их знакомства
        for(int i = 0; i < amountOfPlayers; i++)
        {
            if (players.get(i).getRole().equals("Мафия"))
                mafiaList.append(players.get(i).getName()).append("  |  ");
        }

        System.out.println("Город засыпает, просыпается мафия!");
        for(int i = 0; i < amountOfPlayers; i++)
        {
            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Город засыпает, просыпается мафия!");
            if(players.get(i).getRole().equals("Мафия"))
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Знакомьтесь, мафия: " + mafiaList.toString());
            }
            else if(players.get(i).getRole().equals("Комиссар"))
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Вы комиссар, спите");
            }
            else 
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Вы житель, спите");
            }
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////начало игрового цикла
        Pattern pattern = Pattern.compile("\\|.+\\|");//regex для удаления ника из сообщения
        while(!gameIsOver) {
            ArrayList<String> votes = new ArrayList<>();//массив голосов
            System.out.println("Город просыпается, пора обсудить, кого выгнать! Чтобы проголосовать, введите /имяигрока");
            for(int i = 0; i < amountOfPlayers; i++)
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Город просыпается, пора обсудить, кого выгнать! Чтобы проголосовать, введите /имяигрока");
            }
            int count = 0;

            while (count < amountOfPlayers) {
                String msg = receiveMessage();
                Matcher matcher = pattern.matcher(msg);
                if (msg.contains("/"))
                {
                    if(matcher.find())
                        msg = matcher.replaceAll("");
                    votes.add(msg.replace("/", ""));
                    count++;
                    System.out.println("Осталось " + (amountOfPlayers -count) + " голосов");
                    for (int i = 0; i < amountOfPlayers; i++)
                    {
                        sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Осталось " + (amountOfPlayers - count) + " голосов");
                    }
                }
                else
                {
                    for (int i = 0; i < amountOfPlayers; i++)
                    {
                        sendMessage(players.get(i).getAddress(), players.get(i).getPort(), msg);
                    }
                }
            }

            System.out.println("Голосование завершено! Выбывает игрок: " + getMaxFreqName(votes));
            String roleOfKilled = null;
            for(int i = 0; i < amountOfPlayers; i++)//удаление игрока и сообщение его роли
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Голосование завершено! Выбывает игрок: " + getMaxFreqName(votes));
            }

            Client killedPlayer = getPlayerByName(getMaxFreqName(votes));

            if(killedPlayer.getRole().equals("Мафия"))
            {
                roleOfKilled = killedPlayer.getName() + " был мафией!";
                amountOfMafia--;
                amountOfPlayers--;
            }
            else if(killedPlayer.getRole().equals("Комиссар"))
            {
                roleOfKilled = killedPlayer.getName() + " был комиссаром!";
                amountOfPlayers--;
            }
            else
            {
                roleOfKilled = killedPlayer.getName() + " был гражданином!";
                amountOfPlayers--;
            }
            System.out.println(roleOfKilled);

            for(int j = 0; j < players.size(); j++)
            {
                sendMessage(players.get(j).getAddress(), players.get(j).getPort(), roleOfKilled);
            }
            players.remove(killedPlayer);

            killedPlayer = null;
            if(amountOfMafia ==0 || amountOfPlayers == amountOfMafia)
                break;
            System.out.println("Город засыпает, просыпается мафия, чтобы выбрать жертву...");
            for(int i = 0; i < amountOfPlayers; i++)
            {
                if(players.get(i).getRole().equals("Мафия"))
                    sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Город засыпает, просыпается мафия, чтобы выбрать жертву... Мафия, обсуждайте в чате и голосуйте /имяигрока");
                else
                    sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Город засыпает, просыпается мафия, чтобы выбрать жертву...");
            }
            votes = new ArrayList<>();
            count=0;
            while (count < amountOfMafia) {
                String msg = receiveMessage();
                if (msg.contains("/"))
                {
                    Matcher matcher = pattern.matcher(msg);
                    if(matcher.find())
                        msg = matcher.replaceAll("");
                    votes.add(msg.replace("/", ""));
                    count++;
                    amountOfPlayers--;
                    for (int i = 0; i < players.size(); i++)
                    {
                        if(players.get(i).getRole().equals("Мафия"))
                            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Осталось " + (amountOfMafia - count) + " голосов");
                    }
                }
                else
                {
                    for (int i = 0; i < amountOfPlayers; i++)
                    {
                        if(players.get(i).getRole().equals("Мафия"))
                            sendMessage(players.get(i).getAddress(), players.get(i).getPort(), msg);
                    }
                }
            }
            roleOfKilled =getMaxFreqName(votes);
            killedPlayer = getPlayerByName(roleOfKilled);
            for(int i = 0; i < amountOfPlayers; i++)//жертва умирает
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Убит игрок: " + roleOfKilled);
            }
            players.remove(killedPlayer);

            if(amountOfMafia == 0 || amountOfPlayers == amountOfMafia)
                break;

            if(getComissar(players)!=null)
            {
                Client comissar = getComissar(players);
                System.out.println("Мафия засыпает, просыпается комиссар, чтобы проверить кого-то");
                sendMessage(comissar.getAddress(), comissar.getPort(), "Мафия засыпает, просыпается комиссар, чтобы проверить кого-то. Комиссар, введите имя игрока, которого вы хотите проверить, с помощью /имяигрока");
                for(int i = 0; i < amountOfPlayers; i++)
                {
                    if(players.get(i)!=comissar)
                    {
                        sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Мафия засыпает, просыпается комиссар, чтобы проверить кого-то");
                    }
                }
                String msg = receiveMessage();
                while(!msg.contains("/"))
                {
                    msg = receiveMessage();
                }
                Matcher matcher = pattern.matcher(msg);
                msg = matcher.replaceAll("");
                msg = msg.replace("/", "");

                sendMessage(comissar.getAddress(), comissar.getPort(), "Голосование завершено! Вы проверили игрока: " + msg + ". Его роль: " +getPlayerByName(msg).getRole());
            }
            if(amountOfMafia==0 || amountOfPlayers<=amountOfMafia)
                gameIsOver = true;
        }

        if(amountOfMafia == 0)
        {
            System.out.println("Игра окончена! Победили жители");
            for(int i = 0; i < amountOfPlayers; i++)
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Игра окончена! Победили жители");
            }
        }
        else if(amountOfPlayers<=amountOfMafia)
        {
            System.out.println("Игра окончена! Победила мафия");
            for(int i = 0; i < amountOfPlayers; i++)
            {
                sendMessage(players.get(i).getAddress(), players.get(i).getPort(), "Игра окончена! Победила мафия");
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
        return new String(packet.getData()).replace("\0", "");
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

    public static Client getPlayerByName(String name)
    {
        for(Client p: players)
        {
            if(p.getName().equals(name))
                return p;
        }
        return null;
    }

    public static Client getComissar(ArrayList<Client> players)
    {
        for(Client p: players)
        {
            if(p.getRole().equals("Комиссар"))
                return p;
        }
        return null;
    }
}
