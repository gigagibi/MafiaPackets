import java.util.ArrayList;
import java.util.Scanner;

class Obj
{
    private static String message;

    public Obj(String msg) {
        message = msg;
    }

    public String getMessage()
    {
        return message;
    }
}
public class Test {
    public static void main(String[] args) {
        /*ArrayList<Obj> messagess = new ArrayList<>();
        Scanner sc = new Scanner(System.in);
        for(int i = 0; i < 2; i++)
        {
            String msg = sc.nextLine();
            messagess.add(new Obj(msg));
        }
        for(int i = 0; i < 2; i++)
        {
            System.out.println(messagess.get(i).getMessage());
        }*/
        String a = "Абуб";
        String a1 = "Абуб";
        System.out.println(a == "Абуб");
    }
}
