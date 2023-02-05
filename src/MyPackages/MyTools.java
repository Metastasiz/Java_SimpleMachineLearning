package MyPackages;

import java.util.ArrayList;

public class MyTools {
    public static int myRandom(int lower, int upper){
        int out = (int)(Math.random()*(1+upper-lower))+lower;
        return out;
    }
    //
    private static ArrayList<String> myStringL = new ArrayList<>();
    public static void addStringL(String a){myStringL.add(a);}
    public static void addStringL(int a){myStringL.add(Integer.toString(a));}
    public static ArrayList<String> getStringL(){return myStringL;}
    //
    public class AllCounter{
        private int counter;
        AllCounter(int init){counter = init;}
        public synchronized void add(int a){
            counter +=a;
        }
    }
    public static class MyCounter{
        private static int counter = 0;
        public synchronized static int getAssign(){return counter++;}
    }
    public static void main(String[] arg){
        int a = MyCounter.getAssign();
        int b = MyCounter.getAssign();
        System.out.println(a);
        System.out.println(b);
        for (int i = 0; i < 100; i++){
            System.out.println(MyTools.myRandom(0,10));
        }
    }
}
