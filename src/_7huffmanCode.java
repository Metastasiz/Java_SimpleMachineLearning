import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.PriorityQueue;

public class task7 {
    static int _length;
    static int _distinct;
    static String[] arr;
    static Map<Character,Integer> map = new HashMap<>();
    static Map<Character,Integer> map2 = new HashMap<>();
    static String cur = "";
    static HuffmanNode root = null;
    public static void main(String[] arg){
        String s = "alla ma kota";
        System.out.println(s);
        getString(s);
        System.out.println(_length);
        System.out.println(_distinct);
        //map.entrySet().stream().forEach(e->System.out.println(e.getKey() + " " + e.getValue()));
        System.out.println();
        getHuffmanNode();
        HuffmanPrinter.printCode(root, "");
    }
    static void getString(String in){
        _length = in.length();
        _distinct = (int) in.chars().distinct().count();
        arr = in.chars().distinct().mapToObj(e->Character.toString((char)e)).collect(Collectors.toList()).toArray(String[]::new);
        System.out.println(Arrays.toString(arr));
        for (int i = 0; i < _length; i++){
            char c = in.charAt(i);
            if (!map.containsKey(c))map.put(c,1);
            else {map.put(c,map.get(c)+1);}
        }
        for (Map.Entry<Character,Integer> e : map.entrySet()){
            cur+=String.valueOf(e.getKey())+e.getValue();
        }
        map2 = map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
        System.out.println(cur);
    }
    static char[] getHuffCharArr(){
        char[] out = new char[map.size()];
        int c = 0;
        for (Map.Entry e : map.entrySet()){
            out[c++] = (char)e.getKey();
        }
        return out;
    }
    static int[] getHuffIntArr(){
        int[] out = new int[map.size()];
        int c = 0;
        for (Map.Entry e : map.entrySet()){
            out[c++] = (int)e.getValue();
        }
        return out;
    }
    static void getHuffmanNode(){
        int n = _distinct;
        char[] charArr = getHuffCharArr();
        int[] charFreq = getHuffIntArr();
        System.out.println(Arrays.toString(charArr));
        System.out.println(Arrays.toString(charFreq));
        PriorityQueue<HuffmanNode> q = new PriorityQueue<>(n, new ImplementComparator());
        for (int i = 0; i < n; i++) {
            HuffmanNode hn = new HuffmanNode();
            hn.c = charArr[i];
            hn.item = charFreq[i];
            hn.left = null;hn.right = null;
            q.add(hn);
        }
        while (q.size() > 1) {
            HuffmanNode x = q.peek();q.poll();
            HuffmanNode y = q.peek();q.poll();
            HuffmanNode f = new HuffmanNode();
            f.item = x.item + y.item;
            f.c = '-';
            f.left = x;f.right = y;
            root = f;
            q.add(f);
        }
    }
}
class HuffmanNode {
    int item;
    char c;
    HuffmanNode left;
    HuffmanNode right;
}
class ImplementComparator implements Comparator<HuffmanNode> {
    public int compare(HuffmanNode x, HuffmanNode y) {
        return x.item - y.item;
    }
}
class HuffmanPrinter {
    public static void printCode(HuffmanNode root, String s) {
        if (root.left == null && root.right == null) {
            System.out.println(root.c + " = " + s);
            return;
        }
        printCode(root.left, s + "0");
        printCode(root.right, s + "1");
    }
}
