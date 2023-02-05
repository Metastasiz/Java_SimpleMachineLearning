import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class _2perceptronModel {
    static double learningRate = 0.2;
    static final int rounds = 100;
    static final int itterations = 100;
    static final boolean debug = false;
    static final String inputPath_test = "src/test.xlsx";
    static final String inputPath_train = "src/train.xlsx";
    static final String outputPath_test = "src/out.xlsx";
    static final String instruction = "exit and done commands";
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
    static List<Map<String, String>> dataInput = new ArrayList<>();
    static List<Map<String, String>> dataInputNorm = new ArrayList<>();
    //
    static Map<String,Map<String,String>> currentWeight = new HashMap<>();
    //
    static Map<String,String> prediction = new HashMap<>();
    //
    public static void main(String[] arg) {
        Scanner scan = new Scanner(System.in);
        //
        loadTrain();
        processMxMnDis();
        normaliseTrain();
        //
        loadWeight();
        trainAllWeight();
        //
        loadTest();
        normaliseTest();
        //
        predictTest();
        showPrediction();
        String input;
        System.out.println(instruction);
        while (!(input = scan.nextLine()).equalsIgnoreCase("exit")){
            System.out.println("Please enter your input");
            while (!(input = scan.nextLine()).equalsIgnoreCase("done")){
                String[] arr = input.split(" ");
                Map<String, String> tmp = new HashMap<>();
                for (int i = 0; i < arr.length; i++){
                    //System.out.println(i);
                    if (!isNumeric(arr[i]) || i >= dataTrain.get(0).size()-1){
                        break;}
                    tmp.put(String.valueOf(i),String.valueOf(arr[i]));

                }
                dataInput.add(tmp);
            }
            normaliseCustom(dataInput);
            predictCustom(dataInputNorm);
            showCustom();
        }

    }
    public static boolean isNumeric(String a){
        try{
            Double.parseDouble(a);
            return true;
        } catch (NumberFormatException e){
            return false;
        }
    }
    public static void loadTrain() {
        try {
            FileInputStream inputFile = new FileInputStream(inputPath_train);
            Workbook workbook = new XSSFWorkbook(inputFile);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheets = workbook.sheetIterator();
            while (sheets.hasNext()) {
                Sheet thisSheet = sheets.next();
                //System.out.println("Sheet name is " + thisSheet.getSheetName());
                Iterator<Row> rows = thisSheet.iterator();
                while (rows.hasNext()) {
                    Row thisRow = rows.next();
                    Iterator<Cell> cells = thisRow.iterator();
                    Map<String, String> row = new HashMap<>();
                    while (cells.hasNext()) {
                        Cell cell = cells.next();
                        String value = dataFormatter.formatCellValue(cell).trim().replace(",", ".");
                        row.put(String.valueOf(row.size()), value);
                        //System.out.print(value + "\t");
                    }
                    dataTrain.add(row);
                    //System.out.println();
                }
            }
            //
            if (debug) {
                for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
                    Map<String, String> tmp = iter.next();
                    tmp.entrySet()
                            .stream()
                            .forEach(e -> {
                                System.out.print(e.getKey() + "_" + e.getValue() + "\t");
                            });
                    System.out.println();
                }
            }
            //
            System.out.println("Training data has been uploaded");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void processMxMnDis(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            tmp.entrySet()
                    .stream()
                    .filter(e -> !e.getKey().equalsIgnoreCase(String.valueOf(tmp.size() - 1)))
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
    public static void normaliseCustom(List<Map<String, String>> input){
        for (Iterator<Map<String, String>> iter = input.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            Map<String, String> out = new HashMap<>();
            for (int i = 0; i < tmp.size()-1; i++){
                double value = ((Double.valueOf(tmp.get(String.valueOf(i))) - Double.valueOf(dataTrainMin.get(String.valueOf(i)))) / Double.valueOf(dataTrainDis.get(String.valueOf(i))) * 2)-1;
                out.put(String.valueOf(i),String.valueOf(value));
            }
            out.put(String.valueOf(tmp.size()-1),tmp.get(String.valueOf(tmp.size()-1)));
            dataInputNorm.add(out);
        }
        if (debug) {
            for (Iterator<Map<String, String>> iter = dataInputNorm.iterator(); iter.hasNext(); ) {
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
    public static void normaliseTrain(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            Map<String, String> out = new HashMap<>();
            for (int i = 0; i < tmp.size()-1; i++){
                double value = ((Double.valueOf(tmp.get(String.valueOf(i))) - Double.valueOf(dataTrainMin.get(String.valueOf(i)))) / Double.valueOf(dataTrainDis.get(String.valueOf(i))) * 2)-1;
                out.put(String.valueOf(i),String.valueOf(value));
            }
            out.put(String.valueOf(tmp.size()-1),tmp.get(String.valueOf(tmp.size()-1)));
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
    public static void loadWeight(){
        for (Iterator<Map<String, String>> iter = dataTrain.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            String a = tmp.get(String.valueOf(tmp.size()-1));
            if (!currentWeight.containsKey(a)){
                currentWeight.put(a,new HashMap<>());
                for (int i = 0; i < tmp.size()-1; i++){
                    currentWeight.get(a).put(String.valueOf(i),String.valueOf(1));
                }
                currentWeight.get(a).put(String.valueOf(currentWeight.get(a).size()),String.valueOf(1));
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
    public static double getTH(String decision, int a, List<Map<String, String>> list){
        Map<String,String> tmp = list.get(a);
        double m121 = 0;
        double output = 0;
        for (int i = 0; i < tmp.size()-1; i++){
            double next = Double.valueOf(tmp.get(String.valueOf(i)))*Double.valueOf(currentWeight.get(decision).get(String.valueOf(i)));
            m121 += next;
        }
        //constant
        double constant = Double.valueOf(currentWeight.get(decision).get(String.valueOf(currentWeight.get(decision).size()-1)));
        return output = m121+constant;
    }
    public static void trainAllWeight(){
        currentWeight.entrySet()
                .stream()
                .forEach(e -> trainWeight(e.getKey()));
    }
    public static void trainWeight(String decision){
        for (int r = 0; r < rounds; r++){
            for (int i = 0; i < dataTrainNorm.size(); i++){
                //System.out.println("Training now...\t" + i);
                int counter = 0;
                while (counter < itterations){
                    double TH = getTH(decision,i,dataTrainNorm);
                    Map<String, String> tmp = dataTrainNorm.get(i);
                    if (TH>=0 &&  tmp.get(String.valueOf(tmp.size()-1)).equalsIgnoreCase(decision)){
                        //if (TH < 0) returns 0, desire = 0
                        for (int j = 0; j < currentWeight.get(decision).size()-1;j++){
                            double weight0 = Double.valueOf(currentWeight.get(decision).get(String.valueOf(j)));
                            double learning0 = -1*Double.valueOf(tmp.get(String.valueOf(j)))*learningRate;
                            double weight1 = weight0+learning0;
                            currentWeight.get(decision).put(String.valueOf(j),String.valueOf(weight1));
                        }
                        int size = currentWeight.get(decision).size();
                        double weight0 = Double.valueOf(currentWeight.get(decision).get(String.valueOf(size-1)));
                        double learning0 = -1*(1)*learningRate;
                        double weight1 = weight0+learning0;
                        currentWeight.get(decision).put(String.valueOf(size-1),String.valueOf(weight1));
                    }else if (TH<0  && !tmp.get(String.valueOf(tmp.size()-1)).equalsIgnoreCase(decision)){
                        //if (TH < 0) returns 0, desire = 0,
                        for (int j = 0; j < currentWeight.get(decision).size()-1;j++){
                            double weight0 = Double.valueOf(currentWeight.get(decision).get(String.valueOf(j)));
                            double learning0 = 1*Double.valueOf(tmp.get(String.valueOf(j)))*learningRate;
                            double weight1 = weight0+learning0;
                            currentWeight.get(decision).put(String.valueOf(j),String.valueOf(weight1));
                        }
                        int size = currentWeight.get(decision).size();
                        double weight0 = Double.valueOf(currentWeight.get(decision).get(String.valueOf(size-1)));
                        double learning0 = 1*(1)*learningRate;
                        double weight1 = weight0+learning0;
                        currentWeight.get(decision).put(String.valueOf(size-1),String.valueOf(weight1));
                    }else {
                        break;
                    }
                    counter++;
                    //System.out.println(i);
                }
            }

        }
        //showWeight();

    }

    public static void loadTest() {
        try {
            FileInputStream inputFile = new FileInputStream(inputPath_test);
            Workbook workbook = new XSSFWorkbook(inputFile);
            DataFormatter dataFormatter = new DataFormatter();
            Iterator<Sheet> sheets = workbook.sheetIterator();
            while (sheets.hasNext()) {
                Sheet thisSheet = sheets.next();
                //System.out.println("Sheet name is " + thisSheet.getSheetName());
                Iterator<Row> rows = thisSheet.iterator();
                while (rows.hasNext()) {
                    Row thisRow = rows.next();
                    Iterator<Cell> cells = thisRow.iterator();
                    Map<String, String> row = new HashMap<>();
                    while (cells.hasNext()) {
                        Cell cell = cells.next();
                        String value = dataFormatter.formatCellValue(cell).trim().replace(",", ".");
                        row.put(String.valueOf(row.size()), value);
                        //System.out.print(value + "\t");
                    }
                    dataTest.add(row);
                    //System.out.println();
                }
            }
            //
            if (debug) {
                for (Iterator<Map<String, String>> iter = dataTest.iterator(); iter.hasNext(); ) {
                    Map<String, String> tmp = iter.next();
                    tmp.entrySet()
                            .stream()
                            .forEach(e -> {
                                System.out.print(e.getKey() + "_" + e.getValue() + "\t");
                            });
                    System.out.println();
                }
            }
            //
            System.out.println("Testing data has been uploaded");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void normaliseTest(){
        for (Iterator<Map<String, String>> iter = dataTest.iterator(); iter.hasNext(); ) {
            Map<String, String> tmp = iter.next();
            Map<String, String> out = new HashMap<>();
            for (int i = 0; i < tmp.size()-1; i++){
                double value = ((Double.valueOf(tmp.get(String.valueOf(i))) - Double.valueOf(dataTrainMin.get(String.valueOf(i)))) / Double.valueOf(dataTrainDis.get(String.valueOf(i))) * 2)-1;
                out.put(String.valueOf(i),String.valueOf(value));
            }
            out.put(String.valueOf(tmp.size()-1),tmp.get(String.valueOf(tmp.size()-1)));
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
    public static void predictTest(){
        for (int i = 0; i < dataTestNorm.size(); i++){
            double lowest = getTH(dataTestNorm.get(0).get(String.valueOf(dataTestNorm.get(0).size()-1)),i,dataTestNorm);
            //System.out.println(lowest);
            prediction.put(String.valueOf(i),dataTestNorm.get(0).get(String.valueOf(dataTestNorm.get(0).size()-1)));
            for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
                double tmp = getTH(e.getKey(),i,dataTestNorm);
                if (debug)System.out.println(i+"\t"+e.getKey() + "\t"+tmp);
                if (tmp < lowest){
                    lowest = tmp;
                    prediction.put(String.valueOf(i),e.getKey());
                }
            }
        }
    }
    public static void predictCustom(List<Map<String,String>> input){
        prediction = new HashMap<>();
        for (int i = 0; i < input.size(); i++){
            double lowest = getTH(dataTrainNorm.get(0).get(String.valueOf(dataTestNorm.get(0).size()-1)),i,input);
            prediction.put(String.valueOf(i),dataTrainNorm.get(0).get(String.valueOf(dataTestNorm.get(0).size()-1)));
            for (Map.Entry<String, Map<String,String>> e : currentWeight.entrySet()) {
                double tmp = getTH(e.getKey(),i,input);
                if (debug)System.out.println(i+"\t"+e.getKey() + "\t"+tmp);
                if (tmp < lowest){
                    lowest = tmp;
                    prediction.put(String.valueOf(i),e.getKey());
                }
            }
        }
    }
    public static void showPrediction(){
        double total = 0;
        for (int i = 0; i < prediction.size(); i++){
            System.out.println(i + "\tprediction =\t" + prediction.get(String.valueOf(i))+"\tactual =\t" + dataTest.get(i).get(String.valueOf(dataTest.get(i).size()-1)));
            if (prediction.get(String.valueOf(i)).equalsIgnoreCase(dataTest.get(i).get(String.valueOf(dataTest.get(i).size()-1)))){
                total++;
            }
        }
        System.out.println("Accuracy of\t"+(total/prediction.size())*100+"%");
    }
    public static void showCustom(){
        System.out.println("Predicting...");
        prediction.entrySet()
                .stream().forEach(e->System.out.println(e.getKey() + "\tprediction = " + e.getValue()));
    }
}
