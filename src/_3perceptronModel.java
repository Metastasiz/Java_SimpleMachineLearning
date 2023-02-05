import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class _3perceptronModel {
    static final String[] pathname = new String[]{"hw", "pl", "sv"};
    static double learningRate = 0.5;
    static final int rounds = 50;
    static final int itterations = 99999;
    static final double startingWeight = 1;
    static final String constant = "constant";
    static boolean debug = false;
    static final double errorTH = 0.02;
    static final int inputSize = 30;
    //
    static final String _decision = "decision";
    static final String pathTest = "src/nai3_test/";
    static final String pathTrain = "src/nai3_train/";
    static final String filter = "abcdefghijklmnopqrstuvwxyz";
    //
    static List<Map<String, String>> dataTrain = new ArrayList<>();
    static List<Map<String, String>> dataTrainNorm = new ArrayList<>();
    //
    static Map<String, String> dataTrainMax = new HashMap<>();
    static Map<String, String> dataTrainMin = new HashMap<>();
    static Map<String, String> dataTrainDis = new HashMap<>();
    //
    static List<Map<String, String>> dataTest = new ArrayList<>();
    static List<Map<String, String>> dataTestNorm = new ArrayList<>();
    //
    static Map<String,Map<String,String>> currentWeight = new HashMap<>();
    //
    static Map<String,String> prediction = new HashMap<>();
    //
    public static void reset(){
        dataTest = new ArrayList<>();
        dataTestNorm = new ArrayList<>();
        prediction = new HashMap<>();
    }
    public static String stringFromPath(String path){
        String out = "";
        try{
            //System.out.println(path);
            out = new String(Files.readAllBytes(Paths.get(path))).replaceAll("[^a-zA-Z]","").toLowerCase(Locale.ROOT);
        } catch(Exception e){}
        return out;
    }
    public static Map<String, String> countCharString(String in, String decision){
        Map<String, String> out = new HashMap<>();
        for (int i = 0; i < filter.length(); i++){
            String tmp = String.valueOf(filter.charAt(i));
            out.put(tmp,"0");
        }
        for (int i = 0; i < in.length(); i++){
            String tmp = String.valueOf(in.charAt(i));
            out.put(tmp,String.valueOf(Double.valueOf(out.get(tmp))+1));
        }
        out.entrySet()
                .stream()
                .forEach(e -> out.put(e.getKey(),String.valueOf(Double.valueOf(e.getValue())/in.length())));
        out.put(_decision,decision);
        //System.out.println(out);
        return out;
    }
    public static void loadCustom(String in){
        in = in.replaceAll("[^a-zA-Z]","").toLowerCase(Locale.ROOT);
        dataTest.add(countCharString(in,"blank"));
    }
    public static void loadTrain(){
        final int trainSize = 10;
        for (String a : pathname){
            String thisPath = pathTrain + a;
            for (int i = 0; i < trainSize; i++){
                String in = stringFromPath(thisPath + "/" + i);
                dataTrain.add(countCharString(in,a));
            }
        }
    }
    public static void getMxMn(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            tmp.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase(_decision))
                    .forEach(e -> {
                        if (!dataTrainMax.containsKey(e.getKey())) {
                            dataTrainMax.put(e.getKey(), e.getValue());
                        } else if (Double.valueOf(e.getValue()) > Double.valueOf(dataTrainMax.get(e.getKey()))) {
                            dataTrainMax.put(e.getKey(), e.getValue());
                        }
                        if (!dataTrainMin.containsKey(e.getKey())) {
                            dataTrainMin.put(e.getKey(), e.getValue());
                        } else if (Double.valueOf(e.getValue()) < Double.valueOf(dataTrainMin.get(e.getKey()))) {
                            dataTrainMin.put(e.getKey(), e.getValue());
                        }
                    });
        }
        if (debug){
            dataTrainMax.entrySet()
                    .stream()
                    .forEach(e -> {
                        System.out.println(e);
                    });
            dataTrainMin.entrySet()
                    .stream()
                    .forEach(e -> {
                        System.out.println(e);
                    });
        }
        dataTrainMax.entrySet()
                .stream()
                .forEach(e -> {
                    dataTrainDis.put(e.getKey(),
                            String.valueOf(
                                    Double.valueOf(dataTrainMax.get(e.getKey()))
                                            - Double.valueOf(dataTrainMin.get(e.getKey())))
                    );
                });
    }
    public static void normTrain(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            Map<String, String> out = new HashMap<>();
            tmp.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase(_decision))
                    .forEach(e -> {
                        double value = ((Double.valueOf(e.getValue()) - Double.valueOf(dataTrainMin.get(e.getKey()))) / Double.valueOf(dataTrainDis.get(e.getKey())) * 2)-1;
                        out.put(e.getKey(),String.valueOf(value));
                    });
            out.put(_decision,tmp.get(_decision));
            dataTrainNorm.add(out);
        }
        if (debug) {
            for (Iterator<Map<String, String>> iter = dataTrainNorm.iterator(); iter.hasNext(); ) {
                Map<String, String> tmp = iter.next();
                tmp.entrySet()
                        .stream()
                        .forEach(e -> {
                            System.out.print(e.getKey() + "_" + e.getValue() + "\t");
                        });
                System.out.println();
            }
        }
    }
    public static void loadTest(){
        final int trainSize = inputSize;
        String thisPath = pathTest;
        for (int i = 0; i < trainSize; i++){
            String in = stringFromPath(thisPath + "/" + i);
            String d = pathname[2];
            if (i < 10) d = pathname[0];
            else if ( i < 20) d = pathname[1];
            dataTest.add(countCharString(in,d));
        }
    }
    public static void normTest(){
        for (Iterator<Map<String, String>> iter = dataTest.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            Map<String, String> out = new HashMap<>();
            tmp.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase(_decision))
                    .forEach(e -> {
                        double value = ((Double.valueOf(e.getValue()) - Double.valueOf(dataTrainMin.get(e.getKey()))) / Double.valueOf(dataTrainDis.get(e.getKey())) * 2)-1;
                        out.put(e.getKey(),String.valueOf(value));
                    });
            out.put(_decision,tmp.get(_decision));
            dataTestNorm.add(out);
        }
        if (debug) {
            for (Iterator<Map<String, String>> iter = dataTestNorm.iterator(); iter.hasNext(); ) {
                Map<String, String> tmp = iter.next();
                tmp.entrySet()
                        .stream()
                        .forEach(e -> {
                            System.out.print(e.getKey() + "_" + e.getValue() + "\t");
                        });
                System.out.println();
            }
        }
    }


    public static void createWeight(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            String a = tmp.get(_decision);
            if (!currentWeight.containsKey(a)){
                currentWeight.put(a,new HashMap<>());
                tmp.entrySet()
                        .stream()
                        .filter(e -> !e.getKey().equalsIgnoreCase(_decision))
                        .forEach(e -> currentWeight.get(a).put(e.getKey(),String.valueOf(startingWeight)));
                currentWeight.get(a).put(constant,String.valueOf(startingWeight));
            }
        }
        if (debug) {
            for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
                System.out.println(e);
            }
        }
    }
    public static void showWeight(){
        for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
            System.out.println(e);
        }
    }
    public static double getNet(String decision, int a, List<Map<String, String>> list){
        Map<String,String> tmp = list.get(a);
        double output = 0;
        for (Map.Entry<String, String> e : tmp.entrySet()) {
            if (e.getKey().equalsIgnoreCase(_decision)){
                continue;
            }
            double next = Double.valueOf(e.getValue())*Double.valueOf(currentWeight.get(decision).get(e.getKey()));
            output += next;

        }
        //constant
        output += Double.valueOf(currentWeight.get(decision).get(constant));
        return output;
    }
    public static void trainAllWeight(){
        currentWeight.entrySet()
                .stream()
                .forEach(e -> trainWeight(e.getKey()));
    }
    public static void trainWeight(String decision){
        //if (decision.equalsIgnoreCase("rn"))debug = true;else {debug = false;}
        if(debug)System.out.println("\n\n\n\n\n\n\n\n\n\nTraining now..."+decision);
        for (int r = 0; r < rounds; r++){
            //System.out.println(r);
            double p = (0.+r)/rounds*100;
            if(debug)System.out.println(p + "%...");
            for (int i = 0; i < dataTrainNorm.size(); i++){
                {
                    //if(decision.equalsIgnoreCase("rn"))System.out.println(dataTrainNorm.get(i));
                    //if(debug)System.out.println("Training now..."+decision+"\t" + i +" = "+dataTrainNorm.get(i).get(_decision));
                }
                Map<String, String> tmp = dataTrainNorm.get(i);
                double net = getNet(decision,i,dataTrainNorm);
                int counter = 0;
                if ((Math.abs(-1-net)<errorTH&&!tmp.get(_decision).equalsIgnoreCase(decision))||(Math.abs(1-net)<errorTH&&tmp.get(_decision).equalsIgnoreCase(decision))){
                    //System.out.println(net);
                    //showWeight();
                    continue;
                }
                while (counter < itterations){
                    net = getNet(decision,i,dataTrainNorm);
                    if(debug){System.out.println("1\t"+net);System.out.println(currentWeight.get(decision));}
                    //
                    if ((net>=0) &&  !tmp.get(_decision).equalsIgnoreCase(decision)){
                        //if (TH < 0) returns 0, desire = 0
                        currentWeight.get(decision).entrySet()
                                .stream()
                                .filter(e -> !e.getKey().equalsIgnoreCase(constant))
                                .forEach(e -> {
                                    double weight0 = Double.valueOf(e.getValue());
                                    double learning0 = -1*Double.valueOf(tmp.get(e.getKey()))*learningRate;
                                    double weight1 = weight0+learning0;
                                    currentWeight.get(decision).put(e.getKey(),String.valueOf(weight1));
                                });
                        double weight0 = Double.valueOf(currentWeight.get(decision).get(constant));
                        double learning0 = -1*(1)*learningRate;
                        double weight1 = weight0+learning0;
                        currentWeight.get(decision).put(constant,String.valueOf(weight1));
                    }else if ((net<0) && tmp.get(_decision).equalsIgnoreCase(decision)){
                        //if (TH < 0) returns 0, desire = 0,
                        currentWeight.get(decision).entrySet()
                                .stream()
                                .filter(e -> !e.getKey().equalsIgnoreCase(constant))
                                .forEach(e -> {
                                    double weight0 = Double.valueOf(e.getValue());
                                    double learning0 = 1*Double.valueOf(tmp.get(e.getKey()))*learningRate;
                                    double weight1 = weight0+learning0;
                                    currentWeight.get(decision).put(e.getKey(),String.valueOf(weight1));
                                });
                        double weight0 = Double.valueOf(currentWeight.get(decision).get(constant));
                        double learning0 = 1*(1)*learningRate;
                        double weight1 = weight0+learning0;
                        currentWeight.get(decision).put(constant,String.valueOf(weight1));
                    }else {
                        break;
                    }
                    //showWeight();
                }
                net = getNet(decision,i,dataTrainNorm);
                if(debug){System.out.println("2\t"+net);System.out.println(currentWeight.get(decision));}
                double max = Math.abs(Double.valueOf(currentWeight.get(decision).get(constant)));
                for (Map.Entry<String, String> e : currentWeight.get(decision).entrySet()) {
                    if (max<Math.abs(Double.valueOf(e.getValue()))){
                        max = Math.abs(Double.valueOf(e.getValue()));
                    }
                }
                for (Map.Entry<String, String> e : currentWeight.get(decision).entrySet()) {
                    currentWeight.get(decision).put(e.getKey(),String.valueOf(Double.valueOf(e.getValue())/max));
                }
                net = getNet(decision,i,dataTrainNorm);
                if(debug){System.out.println("3\t"+net);System.out.println(currentWeight.get(decision));}
                net = Math.abs(net);
                if (net > 1){
                    for (Map.Entry<String, String> e : currentWeight.get(decision).entrySet()) {
                        currentWeight.get(decision).put(e.getKey(),String.valueOf(Double.valueOf(e.getValue())/net));
                    }
                }
                net = getNet(decision,i,dataTrainNorm);
                if(debug){System.out.println("4\t"+net);System.out.println(currentWeight.get(decision));}
                counter++;
                //showWeight();
            }
        }
        double net = getNet(decision,0,dataTrainNorm);
    }
    public static void weightMnMx(){
        Map<String,String> max = new HashMap<>();
        Map<String,String> min = new HashMap<>();
        currentWeight.entrySet().stream()
                .forEach(k -> {
                    k.getValue().entrySet().stream()
                            .forEach(e -> {
                                if (!max.containsKey(k.getKey())){max.put(k.getKey(),e.getValue());}
                                else if (Double.valueOf(max.get(k.getKey()))<Double.valueOf(e.getValue())){
                                    max.put(k.getKey(),e.getValue());
                                }
                                if (!min.containsKey(k.getKey())){min.put(k.getKey(),e.getValue());}
                                else if (Double.valueOf(min.get(k.getKey()))>Double.valueOf(e.getValue())){
                                    min.put(k.getKey(),e.getValue());
                                }
                            });

                });
        //System.out.println(max);
        //System.out.println(min);
    }
    public static void predictTest(){
        for (int i = 0; i < dataTestNorm.size(); i++){
            double highest = getNet(pathname[0],i,dataTestNorm);
            //System.out.println(lowest);
            prediction.put(String.valueOf(i),dataTestNorm.get(0).get(_decision));
            for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
                double tmp = getNet(e.getKey(),i,dataTestNorm);
                if (true)System.out.println(i+"\t"+e.getKey() + "\t"+tmp);
                if (tmp > highest){
                    highest = tmp;
                    prediction.put(String.valueOf(i),e.getKey());
                }
            }
        }
    }
    public static void showPrediction(){
        double total = 0;
        for (int i = 0; i < prediction.size(); i++){
            System.out.println(i + "\tprediction =\t" + prediction.get(String.valueOf(i))+"\tactual =\t" + dataTest.get(i).get(_decision));
            if (prediction.get(String.valueOf(i)).equalsIgnoreCase(dataTest.get(i).get(_decision))){
                total++;
            }
        }
        System.out.println("Accuracy of\t"+(total/prediction.size())*100+"%");
    }
    public static void performance(){
        for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
            double pT = 0;
            double pF = 0;
            double aT = 0;
            double aF = 0;
            double pTaT = 0;
            double correctAns = 0;
            for (int i = 0; i < dataTestNorm.size(); i++){
                if (e.getKey().equalsIgnoreCase(dataTestNorm.get(i).get(_decision))){aT++;}else{aF++;}
                double tmp = getNet(e.getKey(),i,dataTestNorm);
                if (tmp >= 0){pT++;}else {pF++;}
                if (tmp >= 0 && e.getKey().equalsIgnoreCase(dataTestNorm.get(i).get(_decision))){
                    correctAns++;
                    pTaT++;
                } else if (tmp < 0 && !e.getKey().equalsIgnoreCase(dataTestNorm.get(i).get(_decision))){
                    correctAns++;
                }
            }
            System.out.println("FOR "+e.getKey());
            System.out.println("Prediction:\t"+pT + "\t" + pF);
            System.out.println("Actual:\t"+aT + "\t" + aF);
            System.out.println("Accuracy = "+(correctAns/dataTestNorm.size())*100+"%");
            System.out.println("Precision = "+(pTaT/pT)*100+"%");
            System.out.println("Recall = "+(pTaT/aT)*100+"%");
            System.out.println("F-measure = "+(2/((pT/pTaT)+(aT/pTaT)))*100+"%");
        }
    }
    public static void main(String[] arg) {
        System.out.println("test");
        //train session
        loadTrain();
        getMxMn();
        normTrain();
        //weight session
        createWeight();
        trainAllWeight();
        weightMnMx();
        //
        loadTest();
        normTest();
        //
        showWeight();
        predictTest();
        showPrediction();
        //
        performance();
        Scanner scan = new Scanner(System.in);
        String tmp;
        System.out.println("please enter ur input, q to quit");
        while (!(tmp = scan.nextLine()).equalsIgnoreCase("q")){
            reset();
            //
            loadCustom(tmp);
            normTest();
            //
            showWeight();
            predictTest();
            showPrediction();
            //
            System.out.println("please enter ur input, q to quit");
        }
    }
}
