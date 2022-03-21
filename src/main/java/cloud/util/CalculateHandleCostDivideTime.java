package cloud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 14:49
 * @description：计算加工成本
 */
public class CalculateHandleCostDivideTime {

    public List<List<int[]>> getProcessCost(int taskNum, int rapid, int taskSize, int resourceSize) {
        List<List<int[]>> processCost = new ArrayList<>();

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            String pathname ="D:\\Coding\\JavaProject\\multi-agent-v3\\data\\write-with-cost-by" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
//            line = br.readLine();
            do {
                line = br.readLine(); // 一次读入一行数据
                if (line != null) {
                    if (line.length() == 1) {
                        int processNum = Integer.parseInt(line);
                        List<int[]> processDataList = new ArrayList<>();
                        processCost.add(processDataList);
                    } else {
                        String [] strs = line.split(",");
                        int [] cost = new int[resourceSize];
                        for (int i=0; i<resourceSize; i++) {
                            cost[i] = Integer.parseInt(strs[i+resourceSize]);
                        }
                        processCost.get(processCost.size()-1).add(cost);
                    }
                }
            } while ( line != null);
            br.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return processCost;
    }

    public int calculateHandleCost(List<List<int[]>> processCost,int taskNum, int rapid, int taskSize, int resourceSize) {
        int totalCost = 0;

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            String pathname ="D:\\Coding\\JavaProject\\multi-agent-v3\\data\\output" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
//            line = br.readLine();
            do {
                line = br.readLine(); // 一次读入一行数据
                if (line != null) {
                    String [] strs = line.split(" ");

                    int process = Integer.parseInt(strs[1]);
                    int task = Integer.parseInt(strs[2].substring(11));
                    int startTime = Integer.parseInt(strs[3]);
                    int endTime = Integer.parseInt(strs[4]);
                    int resource = Integer.parseInt(strs[5].substring(8));

                    totalCost += processCost.get(task).get(process)[resource];
//                    System.out.println(line);
                }
            } while ( line != null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return totalCost;
    }


    public static void main(String[] args) {
        int taskSize = 20;
        int resourceSize = 20;
        List<Integer> list = new ArrayList<>();

        CalculateHandleCostDivideTime calculateHandleCost = new CalculateHandleCostDivideTime();
        for(int taskNum=1; taskNum<=10; taskNum++) {
            System.out.println(taskNum);
            for (int rapid = 1; rapid <= 10; rapid++) {
                int totalCost = calculateHandleCost.calculateHandleCost(calculateHandleCost.getProcessCost(taskNum, rapid, taskSize, resourceSize),taskNum, rapid, taskSize, resourceSize);
                list.add(totalCost);
                System.out.print(totalCost + " ");
            }
            System.out.println();
        }

        StringBuffer sb = new StringBuffer();

        for (int i=0; i<list.size(); i++) {
            sb.append(list.get(i));
            sb.append(",");
        }
        System.out.println(sb.toString());

    }
}
