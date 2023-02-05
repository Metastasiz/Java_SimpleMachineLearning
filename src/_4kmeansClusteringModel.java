import MyPackages.MyTools;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class _4kmeansClusteringModel {
    static int itteration = 100;
    static final boolean debug = false;
    static int clusterSize = 3;
    static boolean stop = false;
    static boolean doubleStop = false;
    //
    static final String inputPath_train = "src/train.xlsx";
    //
    static List<Map<String, String>> dataTrain = new ArrayList<>();
    static List<Map<String, String>> dataTrainNorm = new ArrayList<>();
    static Map<String,Map<String, String>> dataTrainClustDis = new HashMap<>();
    static Map<Integer,List<Integer>> clusterGrp = new HashMap<>();
    static Map<Integer,List<Integer>> clusterStop = new HashMap<>();
    static Map<Integer,Double> clusterGrpDis = new HashMap<>();
    //
    static Map<String, String> dataTrainMax = new HashMap<>();
    static Map<String, String> dataTrainMin = new HashMap<>();
    static Map<String, String> dataTrainDis = new HashMap<>();
    //
    static Map<String,Map<String, String>> clustPts = new HashMap<>();
    //
    static Map<String,Map<String,String>> currentWeight = new HashMap<>();
    public static void initClustPts(){
        //min+dis/(1+g)
        for (int k = 0; k < clusterSize; k++){
            Map<String, String> tmp = new HashMap<>();
            for (int i = 0; i < dataTrainMin.size(); i++){
                //double min = Double.valueOf(dataTrainMin.get(String.valueOf(i)));
                //double dis = Double.valueOf(dataTrainDis.get(String.valueOf(i)));

                //double c = (2/(dis)(1+clusterSize)*(k+1));
                //double out = min+c;
                //tmp.put(String.valueOf(i),String.valueOf(out));
                tmp.put(String.valueOf(i),String.valueOf(MyTools.myRandom(-1,1)));

            }
            clustPts.put(String.valueOf(k),tmp);
        }
    }
    public static void clearClustPts(){
        for (int j = 0; j < clustPts.size(); j++){
            for (int i = 0; i < clustPts.get(String.valueOf(j)).size(); i++){
                clustPts.get(String.valueOf(j)).put(String.valueOf(i),"0");
            }
        }
    }
    public static void getSumOfSqrtOfDisOfMembers(){
        clusterGrpDis = new HashMap<>();
        for (int k = 0; k < clustPts.size(); k++){
            for (int j = 0; j < dataTrainClustDis.size();j++){
                double nodeDistance = 0;
                if(Double.valueOf(dataTrainClustDis.get(String.valueOf(j)).get(String.valueOf(clustPts.size())))==k){
                    for (int i = 0; i < dataTrainNorm.get(j).size()-1; i++){
                        double disCenter = Double.valueOf(clustPts.get(String.valueOf(k)).get(String.valueOf(i)));
                        double disMember = Double.valueOf(dataTrainNorm.get(j).get(String.valueOf(i)));
                        //assuming distance is
                        double distancePow2 = Math.pow(disCenter-disMember,2);
                        nodeDistance += distancePow2;
                    }
                }
                nodeDistance = Math.sqrt(nodeDistance);
                if (!clusterGrpDis.containsKey(k)){
                    clusterGrpDis.put(k,nodeDistance);

                }else {
                    double prevDis = clusterGrpDis.get(k);
                    clusterGrpDis.put(k,prevDis+nodeDistance);
                }

            }
        }
        System.out.println(clusterGrpDis);
    }
    public static void getClustPts(){
        clearClustPts();
        for (int k = 0; k < clustPts.size(); k++){
            double count = 0;
            for (int j = 0; j < dataTrainClustDis.size();j++){
                if(Double.valueOf(dataTrainClustDis.get(String.valueOf(j)).get(String.valueOf(clustPts.size())))==k){
                    for (int i = 0; i < dataTrainNorm.get(j).size()-1;i++){
                        count++;
                        double cur = Double.valueOf(clustPts.get(String.valueOf(k)).get(String.valueOf(i)));
                        //System.out.println(cur);
                        cur += Double.valueOf(dataTrainNorm.get(j).get(String.valueOf(i)));
                        clustPts.get(String.valueOf(k)).put(String.valueOf(i),String.valueOf(cur));
                    }
                }
            }
            for (int i = 0; i < clustPts.get(String.valueOf(k)).size(); i++){
                double total = Double.valueOf(clustPts.get(String.valueOf(k)).get(String.valueOf(i)));
                if (count!=0) total = total/count;
                clustPts.get(String.valueOf(k)).put(String.valueOf(i),String.valueOf(total));
            }
        }
    }
    public static void calMin(){
        for (int j = 0; j < dataTrainNorm.size(); j++){
            //per data
            for (int k = 0; k < clustPts.size(); k++){
                //per cluster pts
                Map<String, String> tmp = clustPts.get(String.valueOf(k));
                Map<String,String> datatmp = dataTrainNorm.get(j);
                //
                double c = 0;
                for (int i = 0; i < datatmp.size()-1; i++){
                    //per column
                    double d1 = Double.valueOf(datatmp.get(String.valueOf(i)));
                    double d2 = Double.valueOf(tmp.get(String.valueOf(i)));
                    double out = Math.pow((d1-d2),2);
                    c+=out;
                }
                c = Math.sqrt(c);
                if (!dataTrainClustDis.containsKey(String.valueOf(j))){
                    Map<String,String> t = new HashMap<>();
                    t.put(String.valueOf(k),String.valueOf(c));
                    dataTrainClustDis.put(String.valueOf(j),t);
                }
                else {
                    dataTrainClustDis.get(String.valueOf(j)).put(String.valueOf(k),String.valueOf(c));
                }
            }
        }
        for (int i = 0; i < dataTrainClustDis.size();i++){
            double min = Double.valueOf(dataTrainClustDis.get(String.valueOf(i)).get(String.valueOf(0)));
            dataTrainClustDis.get(String.valueOf(i)).put(String.valueOf(clustPts.size()),String.valueOf(0));
            for (int k = 0; k < clustPts.size();k++){
                double current = Double.valueOf(dataTrainClustDis.get(String.valueOf(i)).get(String.valueOf(k)));
                if (current < min){
                    dataTrainClustDis.get(String.valueOf(i)).put(String.valueOf(clustPts.size()),String.valueOf(k));
                }
            }
        }
    }
    public static void compare(){
        for (int i = 0; i < dataTrainClustDis.size(); i ++){
            System.out.print(i+"# ");
            System.out.print(dataTrainNorm.get(i).get(String.valueOf(dataTrainNorm.get(i).size()-1))+" is in group: ");
            System.out.println(dataTrainClustDis.get(String.valueOf(i)).get(String.valueOf(clustPts.size())));

        }
        System.out.println("\n\n\n");
    }
    public static void clusterGroup(){
        for (int k = 0; k < clustPts.size(); k++){
            clusterStop.put(k,new ArrayList<>());
        }
        for (int i = 0; i < dataTrainClustDis.size(); i++){
            int group = Integer.valueOf(dataTrainClustDis.get(String.valueOf(i)).get(String.valueOf(clusterStop.size())));
            clusterStop.get(group).add(i);
        }
        int c = 0;
        //
        CHECK:
        for (int k = 0; k < clusterGrp.size(); k++){
            for (int i = 0; i< clusterGrp.get(k).size();i++){
                if(clusterGrp.get(k).size()!=clusterStop.get(k).size()){break CHECK;}
                if(clusterGrp.get(k).get(i)!=clusterStop.get(k).get(i)){break CHECK;}
            }
            c++;
        }
        if (c==clusterGrp.size()&&clusterGrp.size()!=0)stop=true;
        //
        for (int k = 0; k < clustPts.size(); k++){
            clusterGrp.put(k,new ArrayList<>());
        }
        for (int i = 0; i < dataTrainClustDis.size(); i++){
            int group = Integer.valueOf(dataTrainClustDis.get(String.valueOf(i)).get(String.valueOf(clustPts.size())));
            clusterGrp.get(group).add(i);
        }
        for (int i = 0; i < clusterGrp.size(); i++){
            System.out.println("GROUP " + i + ": ");
            for (Integer e : clusterGrp.get(i)){
                System.out.println(e + "# which is " + dataTrainNorm.get(e).get(String.valueOf(dataTrainNorm.get(e).size()-1)));
            }
        }
    }
    public static void entropy(){
        double totalEnt = 0;
        Map<String,Integer> dataCount = new HashMap<>();
        for (int i = 0; i < dataTrainNorm.size(); i++){
            String key = dataTrainNorm.get(i).get(String.valueOf(dataTrainNorm.get(i).size()-1));
            if (!dataCount.containsKey(key)) {
                dataCount.put(dataTrainNorm.get(i).get(String.valueOf(dataTrainNorm.get(i).size() - 1)),1);
            }else {
                int num = dataCount.get(key);
                dataCount.put(key,num+1);
            }
        }
        List<String> dataType = new ArrayList<>();
        dataCount.entrySet().forEach(e->dataType.add(e.getKey()));
        for (int i = 0; i < dataType.size(); i++){
            double p = (double)(dataCount.get(dataType.get(i)))/dataTrainNorm.size();
            totalEnt -= p*Math.log(p)/Math.log(2);
        }
        System.out.println("Full group entropy: "+totalEnt);
        //
        for (int i = 0; i < clusterGrp.size(); i++){
            double thisEnt = 0;
            Map<String,Integer> countTmp = new HashMap<>();
            //
            for (Integer e : clusterGrp.get(i)){
                String key = dataTrainNorm.get(e).get(String.valueOf(dataTrainNorm.get(e).size()-1));
                if (!countTmp.containsKey(key)){
                    countTmp.put(key,1);
                }else {
                    int num = countTmp.get(key);
                    countTmp.put(key,num+1);
                }
            }
            //
            for (int k = 0; k < dataType.size(); k++){
                if (countTmp.containsKey(dataType.get(k))){
                    double p = (double)(countTmp.get(dataType.get(k)))/clusterGrp.get(i).size();
                    thisEnt -= p*Math.log(p)/Math.log(2);
                }
            }
            System.out.println("GROUP " + i + " entropy: "+thisEnt);
        }
    }
    public static void main(String[] arg){
        Scanner scan = new Scanner(System.in);
        System.out.println("clustersize");
        clusterSize = scan.nextInt();
        System.out.println("min itteration");
        itteration = scan.nextInt();
        //
        loadTrain();
        processMxMnDis();
        normaliseTrain();
        //
        initClustPts();
        //
        for (int i = 0; i < itteration; i++){
            System.out.println("Itteration " + i + ": ");
            calMin();
            getClustPts();
            //compare();
            clusterGroup();
            System.out.println(clustPts);
            getSumOfSqrtOfDisOfMembers();
            System.out.println("\n\n\n");
            if (stop){
                System.out.println("-- SUMMARY --");
                System.out.println("\n");
                break;
            }
        }
        //
        for (int i = 0 ; i < dataTrainClustDis.size(); i++){
            System.out.println(i+"="+dataTrainClustDis.get(String.valueOf(i)));
        }
        System.out.println(clustPts);
        clusterGroup();
        entropy();
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
}
