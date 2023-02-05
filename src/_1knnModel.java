import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class _1knnModel {
    static final boolean debug = false;
    static final String inputPath_test = "src/test.xlsx";
    static final String inputPath_train = "src/train.xlsx";
    static final String outputPath_test = "src/out.xlsx";
    static final String instruction = "type 'quit' to quit/stop input data, 'k' to try different k input on Test data, 't' to test your own input data";
    //
    static List<Map<String, String>> dataTrain = new ArrayList<>();
    static Map<String, String> dataTrainMax = new HashMap<>();
    static Map<String, String> dataTrainMin = new HashMap<>();
    static Map<String, String> dataTrainDis = new HashMap<>();
    //
    static List<Map<String, String>> dataTest = new ArrayList<>();
    static List<Map<String, String>> dataInput = new ArrayList<>();
    static Map<String,Map<Double,String>> dataTestNormalised = new TreeMap<>();
    //
    static Map<String, String> dataKOut = new HashMap<>();
    //
    public static void main(String[] arg) {
        Scanner scan = new Scanner(System.in);
        loadTrain();
        loadTest();
        normaliseTest();
        display(1);
        predictionCustom();
        System.out.println(instruction);
        String input;
        while (!(input = scan.nextLine()).equalsIgnoreCase("quit")){
            if (input.equalsIgnoreCase("k")){
                System.out.println("Enter number of K");
                try {
                    display(Integer.valueOf(scan.nextLine()));
                    predictionCustom();
                } catch (Exception e){System.out.println("unexpected error");}
            } else if (input.equalsIgnoreCase("t")){
                System.out.println(instruction);
                while (!(input = scan.nextLine()).equalsIgnoreCase("quit")) {
                    String[] arr = input.split(" ");
                    Map<String, String> tmp = new HashMap<>();
                    for (int i = 0; i < arr.length; i++){
                        System.out.println(i);
                        if (!isNumeric(arr[i]) || i >= dataTrain.get(0).size()-1){
                            break;}
                        tmp.put(String.valueOf(i),String.valueOf(arr[i]));

                    }
                    dataInput.add(tmp);

                }
                normaliseCustom(dataInput);
                System.out.println("Enter number of K");
                try {
                    display(Integer.valueOf(scan.nextLine()));
                    predictionCustom(dataInput);
                } catch (Exception e){System.out.println("unexpected error");}
            }
            //
            System.out.println(instruction);
        }
        //Map<String, String> rowTrain = new HashMap<>();
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
                    Map<String, String> rowTrain = new HashMap<>();
                    while (cells.hasNext()) {
                        Cell cell = cells.next();
                        String value = dataFormatter.formatCellValue(cell).trim().replace(",", ".");
                        rowTrain.put(String.valueOf(rowTrain.size()), value);
                        //System.out.print(value + "\t");
                    }
                    dataTrain.add(rowTrain);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    Map<String, String> rowTrain = new HashMap<>();
                    while (cells.hasNext()) {
                        Cell cell = cells.next();
                        String value = dataFormatter.formatCellValue(cell).trim().replace(",", ".");
                        rowTrain.put(String.valueOf(rowTrain.size()), value);
                        //System.out.print(value + "\t");
                    }
                    dataTest.add(rowTrain);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void normaliseCustom(List<Map<String, String>> input){
        dataTestNormalised = new TreeMap<>();
        //each test data
        for (int i = 0; i < input.size(); i++){
            Map<String, String> tmp = input.get(i);
            //is pair with all training data
            for (int k = 0; k < dataTrain.size(); k++){
                Double n = 0.;
                //with each of the feature but size limit of tmp.size()-1
                for (int j = 0; j < tmp.size()-1; j++){
                    //System.out.println(i + "_"+k+"_"+j);
                    if (Double.valueOf(dataTrainDis.get(String.valueOf(j)))==0)continue;
                    double a = (Double.valueOf(tmp.get(String.valueOf(j)))
                            -Double.valueOf(dataTrain.get(k).get(String.valueOf(j))
                    ))/Double.valueOf(dataTrainDis.get(String.valueOf(j)));
                    //System.out.println(Double.valueOf(dataTrainDis.get(String.valueOf(j))));
                    //System.out.println(a);
                    n += Math.pow(a,2);
                }
                n = Math.sqrt(n);
                if (!dataTestNormalised.containsKey(String.valueOf(i))){
                    dataTestNormalised.put(String.valueOf(i),new TreeMap<>());
                }
                while (dataTestNormalised.get(String.valueOf(i)).containsKey(n)){
                    if (n == 0){
                        n += 0.00000001;
                    }
                    n *= 1.000001;
                }
                dataTestNormalised.get(String.valueOf(i))
                        .put(n,
                                dataTrain.get(k).get(String.valueOf(dataTrain.get(k).size()-1)));
            }
        }
        //System.out.println("kabooya");
        if(debug) {
            dataTestNormalised.entrySet()
                    .stream()
                    .forEach(e -> {
                        System.out.println(e.getKey() + " ^^ " + e.getValue());
                    });
        }
    }
    public static void normaliseTest(){
        dataTestNormalised = new TreeMap<>();
        //each test data
        for (int i = 0; i < dataTest.size(); i++){
            Map<String, String> tmp = dataTest.get(i);
            //is pair with all training data
            for (int k = 0; k < dataTrain.size(); k++){
                Double n = 0.;
                //with each of the feature but size limit of tmp.size()-1
                for (int j = 0; j < tmp.size()-1; j++){
                    //System.out.println(i + "_"+k+"_"+j);
                    if (Double.valueOf(dataTrainDis.get(String.valueOf(j)))==0)continue;
                    double a = (Double.valueOf(tmp.get(String.valueOf(j)))
                            -Double.valueOf(dataTrain.get(k).get(String.valueOf(j))
                    ))/Double.valueOf(dataTrainDis.get(String.valueOf(j)));
                    //System.out.println(Double.valueOf(dataTrainDis.get(String.valueOf(j))));
                    //System.out.println(a);
                    n += Math.pow(a,2);
                }
                n = Math.sqrt(n);
                if (!dataTestNormalised.containsKey(String.valueOf(i))){
                    dataTestNormalised.put(String.valueOf(i),new TreeMap<>());
                }
                while (dataTestNormalised.get(String.valueOf(i)).containsKey(n)){
                    if (n == 0){
                        n += 0.00000001;
                    }
                    n *= 1.000001;
                }
                dataTestNormalised.get(String.valueOf(i))
                        .put(n,
                                dataTrain.get(k).get(String.valueOf(dataTrain.get(k).size()-1)));
            }
        }
        //System.out.println("kabooya");
        if(debug) {
            dataTestNormalised.entrySet()
                    .stream()
                    .forEach(e -> {
                        System.out.println(e.getKey() + " ^^ " + e.getValue());
                    });
        }
    }
    public static void display(Integer k){
        if (k == null || k == 0){
            k = dataTrain.size()-1;
        }
        dataKOut = new HashMap<>();
        for (int i = 0; i < dataTestNormalised.size(); i++) {
            Map<String, Integer> count = new HashMap<>();
            Map<Double, String> tmp = dataTestNormalised.get(String.valueOf(i));
            int t = 0;
            for (Map.Entry<Double, String> e : tmp.entrySet()){
                if (t > k){break;}
                if (!count.containsKey(e.getValue())){
                    count.put(e.getValue(),1);
                }
                else {
                    count.put(e.getValue(),count.get(e.getValue())+1);
                }
                t++;
            }
            int c = 0;
            for (Map.Entry<String, Integer> j : count.entrySet()) {
                if (j.getValue() > c){
                    dataKOut.put(String.valueOf(i),j.getKey());
                    c = j.getValue();
                }
            }
        }

    }
    public static void predictionCustom(){
        System.out.println("Prediction");
        if (true){
            for (int i = 0; i < dataKOut.size(); i++){
                System.out.println("Number\t" + i + "\tPrediction =\t" + dataKOut.get(String.valueOf(i)) + "\t\t\t" + "Actual =\t" + dataTest.get(i).get(String.valueOf(dataTest.get(i).size()-1)));
            }
        }
        double correct = 0;
        for (int i = 0; i < dataTest.size(); i++){
            String a = dataTest.get(i).get(String.valueOf(dataTest.get(i).size()-1));
            String b = dataKOut.get(String.valueOf(i));
            if (debug){
                System.out.println(a.equalsIgnoreCase(b));
            }
            if (a.equalsIgnoreCase(b)){
                correct++;
            }
        }

        System.out.println("Accuracy of " + correct/dataTest.size()*100. + "%");
    }
    public static void predictionCustom(List<Map<String, String>> input){
        System.out.println("Prediction");
        if (input == null){input = dataTest;}
        if (true){
            for (int i = 0; i < dataKOut.size(); i++){
                System.out.println("Number\t" + i + "\tPrediction =\t" + dataKOut.get(String.valueOf(i)));
            }
        }
        double correct = 0;
        for (int i = 0; i < input.size(); i++){
            String a = input.get(i).get(String.valueOf(input.get(i).size()-1));
            String b = dataKOut.get(String.valueOf(i));
            if (debug){
                System.out.println(a.equalsIgnoreCase(b));
            }
            if (a.equalsIgnoreCase(b)){
                correct++;
            }
        }
    }
}
