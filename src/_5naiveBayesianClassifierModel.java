import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class _5naiveBayesianClassifierModel {
    //5,7 2,8 4,1 1,3
    //6,5 3,2 5,1 2,0
    //5,1 3,8 1,5 0,3
    //Iris-versicolor
    //Iris-virginica
    //Iris-setosa
    static final boolean debug = false;
    static double marginErr = 0;
    static double marginRange = 2;
    //
    static final String inputPath_train = "src/train.xlsx";
    static final String inputPath_test = "src/test.xlsx";
    //
    static List<Map<String, String>> dataTrain = new ArrayList<>();
    static List<Map<String, String>> dataTrainNorm = new ArrayList<>();
    //
    static List<Map<String, String>> dataTest = new ArrayList<>();
    static List<Map<String, String>> dataTestNorm = new ArrayList<>();
    static Map<Integer,String> prediction = new HashMap<>();
    //
    static Map<String, String> dataTrainMax = new HashMap<>();
    static Map<String, String> dataTrainMin = new HashMap<>();
    static Map<String, String> dataTrainDis = new HashMap<>();
    //
    static Map<String,Integer> counter = new HashMap<>();
    static Map<String,Integer> counterD = new HashMap<>();
    static Map<String,Double> counterP = new HashMap<>();
    //
    static List<Map<String, String>> dataInput = new ArrayList<>();
    static List<Map<String, String>> dataInputNorm = new ArrayList<>();
    //
    static void task5(){
        loadTrain();
        processMxMnDis();
        normaliseTrain();
        //
        loadTest();
        normaliseTest();
        countSimilarTest();
        //
        performance();
        //
        Scanner scan = new Scanner(System.in);
        while(true){
            System.out.println("type 'done' to finish input");
            dataInput = new ArrayList<>();
            dataInputNorm = new ArrayList<>();
            //
            String in = "";
            while (!(in=scan.nextLine()).equalsIgnoreCase("done")){
                String[] arr = in.replace(",", ".").split("\s++");
                System.out.println(Arrays.toString(arr));
                input(arr);
            }
            normaliseInput();
            countSimilarInput();
        }
    }
    public static void main(String[] arg){ task5();}
    public static void input(String... in){
        int size = dataTrainNorm.get(0).size()-1;
        int length = in.length>size?size:in.length;
        Map<String,String> out = new HashMap<>();
        for (int i = 0; i < length; i++){
            out.put(String.valueOf(i),in[i]);
        }
        out.put(String.valueOf(length),"no decision");
        dataInput.add(out);
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
    public static void countSimilarTest(){
        for (int k = 0; k < dataTrainNorm.size(); k++){
            int size = Integer.valueOf(dataTrainNorm.get(k).size());
            String decision = dataTrainNorm.get(k).get(String.valueOf(size-1));
            if(!counter.containsKey(decision)){
                counter.put(decision,0);
                counterD.put(decision,0);
                counterP.put(decision,1.);
            }
        }
        for (int i = 0; i < dataTestNorm.size(); i++){
            if(debug)System.out.println(i);
            counterP.entrySet().stream()
                    .forEach(e->counterP.put(e.getKey(),1.));
            for (int j = 0; j < dataTestNorm.get(i).size()-1; j++){
                counterD.entrySet().stream()
                        .forEach(e->counterD.put(e.getKey(),0));
                counter.entrySet().stream()
                        .forEach(e->counter.put(e.getKey(),0));
                double count = 0;
                double test = Double.valueOf(dataTestNorm.get(i).get(String.valueOf(j)));
                for (int k = 0; k < dataTrainNorm.size(); k++){
                    double train = Double.valueOf(dataTrainNorm.get(k).get(String.valueOf(j)));
                    //System.out.println(test);
                    //System.out.println(train);
                    int size = dataTrainNorm.get(k).size();
                    String d = dataTrainNorm.get(k).get(String.valueOf(size-1));
                    int c = counterD.get(d);
                    counterD.put(d,c+1);
                    if (test>=(train-Math.abs(marginRange)* marginErr) && test<=(train+Math.abs(marginRange)* marginErr)){
                        count++;
                        c = counter.get(d);
                        counter.put(d,c+1);
                        //System.out.println("matched");
                    }
                    AtomicBoolean zero = new AtomicBoolean(false);
                    counter.entrySet().stream()
                            .forEach(e->{
                                if(e.getValue()==0){
                                    zero.set(true);
                                }
                            });
                    if (zero.get()){
                        counter.entrySet().stream()
                                .forEach(e->counter.put(e.getKey(),e.getValue()+1));
                        count+=counter.size();
                    }
                }
                counterP.entrySet().stream()
                        .forEach(e->{
                            if(debug)System.out.print((double)counter.get(e.getKey())/counterD.get(e.getKey())+ " ");
                            counterP.put(e.getKey(),e.getValue()*(double)counter.get(e.getKey())/counterD.get(e.getKey()));
                        });
                if(debug)System.out.println();
                //
                if (debug){
                    counter.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    counterD.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    counterP.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    System.out.println(test);
                    System.out.println("count = " + count);
                }
            }
            int tmp = dataTestNorm.get(i).size()-1;
            String out1 = dataTestNorm.get(i).get(String.valueOf(tmp));
            String out = counterP.entrySet().stream().max((e1,e2)->e1.getValue()>e2.getValue()?1:-1).get().getKey();
            prediction.put(i,out);
            System.out.println("Actual: "+ out1 + ", Prediction: " + out);
        }
    }
    public static void normaliseInput(){
        for (Iterator<Map<String, String>> iter = dataInput.iterator(); iter.hasNext(); ) {
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
    public static void countSimilarInput(){
        for (int k = 0; k < dataTrainNorm.size(); k++){
            int size = Integer.valueOf(dataTrainNorm.get(k).size());
            String decision = dataTrainNorm.get(k).get(String.valueOf(size-1));
            if(!counter.containsKey(decision)){
                counter.put(decision,0);
                counterD.put(decision,0);
                counterP.put(decision,1.);
            }
        }
        for (int i = 0; i < dataInputNorm.size(); i++){
            if(debug)System.out.println(i);
            counterP.entrySet().stream()
                    .forEach(e->counterP.put(e.getKey(),1.));
            for (int j = 0; j < dataInputNorm.get(i).size()-1; j++){
                counterD.entrySet().stream()
                        .forEach(e->counterD.put(e.getKey(),0));
                counter.entrySet().stream()
                        .forEach(e->counter.put(e.getKey(),0));
                double count = 0;
                double test = Double.valueOf(dataInputNorm.get(i).get(String.valueOf(j)));
                for (int k = 0; k < dataTrainNorm.size(); k++){
                    double train = Double.valueOf(dataTrainNorm.get(k).get(String.valueOf(j)));
                    //System.out.println(test);
                    //System.out.println(train);
                    int size = dataTrainNorm.get(k).size();
                    String d = dataTrainNorm.get(k).get(String.valueOf(size-1));
                    int c = counterD.get(d);
                    counterD.put(d,c+1);
                    if (test>=(train-Math.abs(marginRange)* marginErr) && test<=(train+Math.abs(marginRange)* marginErr)){
                        count++;
                        c = counter.get(d);
                        counter.put(d,c+1);
                        //System.out.println("matched");
                    }
                    AtomicBoolean zero = new AtomicBoolean(false);
                    counter.entrySet().stream()
                            .forEach(e->{
                                if(e.getValue()==0){
                                    zero.set(true);
                                }
                            });
                    if (zero.get()){
                        counter.entrySet().stream()
                                .forEach(e->counter.put(e.getKey(),e.getValue()+1));
                        count+=counter.size();
                    }
                }
                counterP.entrySet().stream()
                        .forEach(e->{
                            if(debug)System.out.print((double)counter.get(e.getKey())/counterD.get(e.getKey())+ " ");
                            counterP.put(e.getKey(),e.getValue()*(double)counter.get(e.getKey())/counterD.get(e.getKey()));
                        });
                if(debug)System.out.println();
                //
                if (debug){
                    counter.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    counterD.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    counterP.entrySet().stream()
                            .forEach(e->System.out.print(e.getValue()+" "));
                    System.out.println();
                    System.out.println(test);
                    System.out.println("count = " + count);
                }
            }
            int tmp = dataInputNorm.get(i).size()-1;
            String out1 = dataInputNorm.get(i).get(String.valueOf(tmp));
            String out = counterP.entrySet().stream().max((e1,e2)->e1.getValue()>e2.getValue()?1:-1).get().getKey();
            prediction.put(i,out);
            System.out.println("Actual: "+ out1 + ", Prediction: " + out);
        }
    }

    public static void performance(){
        System.out.println();
        double ppT = 0;
        double pnT = 0;
        double npT = 0;
        double nnT = 0;
        double correctT = 0;
        for (Map.Entry<String, Double> e : counterP.entrySet()) {
            String thisCase = e.getKey();
            double pp = 0;
            double pn = 0;
            double np = 0;
            double nn = 0;
            double correct = 0;
            for (int i = 0; i < dataTestNorm.size(); i++) {
                String index = String.valueOf(dataTestNorm.get(i).size() - 1);
                //
                String predict = prediction.get(i);
                String data = dataTestNorm.get(i).get(index);
                boolean actual = predict.equalsIgnoreCase(thisCase);
                boolean classfied = data.equalsIgnoreCase(thisCase);
                if (actual && classfied){ppT++;pp++;correctT++;correct++;}
                if (actual && !classfied){pnT++;pn++;}
                if (!actual && classfied){npT++;np++;}
                if (!actual && !classfied){nnT++;nn++;correctT++;correct++;}
            }
            double p = pp / (pp+np);
            double r = pp / (pp+pn);
            System.out.println("FOR " + e.getKey());
            System.out.println("Classified as -> Positive | Negative");
            System.out.println("Positive:\t\t\t" + pp + "     " + pn);
            System.out.println("Negative:\t\t\t" + np + "     " + nn);
            System.out.println("Accuracy = " + (correct / dataTestNorm.size()) * 100 + "%");
            System.out.println("Precision = " + p * 100 + "%");
            System.out.println("Recall = " + r * 100 + "%");
            System.out.println("F-measure = " + 2/((1/p) + (1/r))*100 + "%");
            System.out.println();
        }
        double p = ppT / (ppT+npT);
        double r = ppT / (ppT+pnT);
        System.out.println("FOR TOTAL");
        System.out.println("Classified as -> Positive | Negative");
        System.out.println("Positive:\t\t\t" + ppT + "     " + pnT);
        System.out.println("Negative:\t\t\t" + npT + "     " + nnT);
        System.out.println("Accuracy = " + (correctT / (counterP.size()*dataTestNorm.size())) * 100 + "%");
        System.out.println("Precision = " + p * 100 + "%");
        System.out.println("Recall = " + r * 100 + "%");
        System.out.println("F-measure = " + 2/((1/p) + (1/r))*100 + "%");
        System.out.println();
    }
}
