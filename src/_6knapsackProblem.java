import MyPackages.MyTools;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class _6knapsackProblem {
    static final boolean debug = true;
    static final String _sizeFilter = "length -";
    static final String _capacityFilter = "capacity";
    static final String _valueFilter = "vals";
    static final String _weightFilter = "sizes";
    static final String _defaultFilter = "\\s*\\d+";
    static final String _arrStart = "\\{";
    static final String _arrEnd = "}";
    static final String _arrSeperator = ",";
    static final String _intArrRegex = _arrStart+"[\\d+"+ _arrSeperator +"\\s*]+"+_arrEnd;
    static final Pattern _intArrPattern = Pattern.compile(_intArrRegex);
    static int _size = 0;
    static boolean _sizeFound = false;
    static int _capacity = 0;
    static boolean _capacityFound = false;
    //
    static final String path = "src/knapsack.txt";
    //
    static final Map<Integer,Integer[]> valueMap = new HashMap<>();
    static final Map<Integer,Integer[]> weightMap = new HashMap<>();

    public static void main(String[] arg){
        reader(path);
        int tmp = MyTools.myRandom(0,valueMap.size()-1);
        System.out.println("Dataset: " + tmp);
        bruteForce(tmp);
        Scanner scan = new Scanner(System.in);
        tmp = scan.nextInt()%valueMap.size();
        System.out.println("Dataset: " + tmp);
        bruteForce(tmp);
    }
    static boolean[] bit(int i){
        int size = _size;
        boolean[] out = new boolean[size];
        String bit = Integer.toBinaryString(i);
        String tmp = "";
        int c = 0;
        for (int j = size-1; j >= 0; j--){
            if (j >= bit.length())tmp+="0";
            else tmp+=bit.charAt(c++);
        }
        for (int j = 0; j < size; j++){
            if (tmp.charAt(j)=='1')out[j]=true;
            else out[j]=false;
        }
        //System.out.println(tmp);
        return out;
    }
    static void check(){
        System.out.println("value");
        valueMap.entrySet().stream().forEach(k->{
            System.out.println(k.getKey());
            System.out.println(Arrays.toString(k.getValue()));
        });
        System.out.println("weight");
        weightMap.entrySet().stream().forEach(k->{
            System.out.println(k.getKey());
            System.out.println(Arrays.toString(k.getValue()));
        });
    }
    static void bruteForce(int n){
        Integer[] weightArr = weightMap.get(n);
        Integer[] valueArr = valueMap.get(n);
        //
        double weight = 0;
        double value = 0;
        double weightPrev = 0;
        double valuePrev = 0;
        int iteration = (int) Math.pow(2,_size);
        //
        boolean[] out = new boolean[0];
        System.out.println("Size = " + _size);
        System.out.println("Iteration = " + iteration);
        long start = System.currentTimeMillis();
        for (int i = 0; i < iteration; i++){
            weight = 0;
            value = 0;
            boolean[] booleanMatrix = bit(i);
            for (int j = 0; j < _size; j++){
                if (booleanMatrix[j]==true){
                    weight+=weightArr[j];
                    value+=valueArr[j];
                }
            }
            if (weight<=_capacity && value>valuePrev){
                out = booleanMatrix;
                weightPrev = weight;
                valuePrev = value;
            }
        }
        {
            //display
            if (debug){
                System.out.println(Arrays.toString(out));
                System.out.println(Arrays.toString(valueArr));
                System.out.println(Arrays.toString(weightArr));
                System.out.print("[");
                for (int i = 0; i < valueArr.length;i++){
                    Double tmp = (double)Math.round((100*valueArr[i]/weightArr[i]))/100;
                    System.out.print(tmp+" ");
                }
                System.out.println("]");
                System.out.println();
                for (int i = 0; i < _size; i++) {
                    if (out[i] == true) {
                        System.out.print("[V=" + valueArr[i] + ",W=");
                        System.out.print(weightArr[i] + "]");
                    }
                }
            }
            System.out.println();
            {
                for (int i = 0; i < out.length; i++){
                    if (out[i]){
                        System.out.print("Index: " + i + " ");
                        System.out.print("Weight: " + weightArr[i] + " ");
                        System.out.print("Value: " + valueArr[i] + " ");
                        System.out.println();
                    }
                }
            }
            System.out.println();
            System.out.println("Max value = " + valuePrev);
            System.out.println("Current weight = " + weightPrev);
            System.out.println();
            long finish = System.currentTimeMillis();
            long timeElapsed = finish - start;
            System.out.println("Time elapsed = " + timeElapsed/1000 + " seconds " + timeElapsed%1000 + " ms");
        }
    }
    static int intFromPattern(String regex, String line){
        Pattern p = Pattern.compile(regex+_defaultFilter);
        Matcher m = p.matcher(line);m.find();
        return Integer.valueOf(m.group(0).replace(regex,"").trim());
    }
    static void reader(String filePath){
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){
            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null){
                if (line.contains(_sizeFilter) && !_sizeFound){
                    _size = intFromPattern(_sizeFilter,line);
                    _sizeFound = true;
                }
                if (line.contains(_capacityFilter) && !_capacityFound){
                    _capacity = intFromPattern(_capacityFilter,line);
                    _capacityFound = true;
                }
                if (line.contains(_valueFilter)){
                    Matcher m = _intArrPattern.matcher(line);m.find();
                    Integer[] arr = Arrays.stream(m.group(0).replaceAll("["+_arrStart+_arrEnd+"]","").split(_arrSeperator)).map(String::trim).map(e->Integer.valueOf(e)).toArray(Integer[]::new);
                    valueMap.put(valueMap.size(),arr);
                }
                if (line.contains(_weightFilter)){
                    Matcher m = _intArrPattern.matcher(line);m.find();
                    Integer[] arr = Arrays.stream(m.group(0).replaceAll("["+_arrStart+_arrEnd+"]","").split(_arrSeperator)).map(String::trim).map(e->Integer.valueOf(e)).toArray(Integer[]::new);
                    weightMap.put(weightMap.size(),arr);
                }
                sb.append(line);
                sb.append(System.lineSeparator());
            }
            //System.out.println(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
