import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class number_guess {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        short target = 50;
        int count = 0;
        boolean firstAsk = true;
        short min = 1;
        short max = 100;
        short inputNum;
        boolean inputError = false;

        while (true) {

            if (firstAsk) {
                target = (short) (random.nextInt(100) + 1);
                System.out.println("目标数已生成，请输入一个[1, 100]的数，或输入10086查看正确答案：");
                firstAsk = false;
            } else if (!inputError) {
                System.out.println("在" + min + "和" + max + "之间，再猜：");
            }

            try {
                inputNum = scanner.nextShort();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("我们在读取你的输入的时候捕获到了一个异常，我懒得管是什么，反正你输入的东西肯定不正常）请你重新输入：");
                inputError = true;
                scanner.nextLine();
                continue;
            }


            inputError = false;

            if (inputNum == 10086) {
                System.out.println("这就放弃了？会变成猫娘的哦~");
                System.out.println("还是告诉你吧~答案是 " + target + " ！");
                break;
            }

            if  (inputNum > max) {System.out.println("沉思……你输入的数怎么比最大值还大？重新输入！");continue;}
            if  (inputNum < min) {System.out.println("沉思……你输入的数怎么比最大值还小？重新输入！");continue;}

            if (inputNum > target) {
                max = inputNum;
                System.out.println("大了哦~");
            } else if (inputNum < target) {
                min = inputNum;
                System.out.println("小了哦~");
            } else {
                System.out.println("!!!");
                //密码的这啥子异常一定要处理不然不给编译
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println("哪个沙子要阻止我sleep？");
                }
                System.out.println("你只用了" + count + "次就猜出了答案，难道说，你真的是天才！！！");

                System.out.println("不想写了，exit吧");
                System.exit(0);

            }
            count++;
        }

    }

}