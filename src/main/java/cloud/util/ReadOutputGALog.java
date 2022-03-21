package cloud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 8:53
 * @description：读取日志
 */
public class ReadOutputGALog {

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


    public Set<String> readGALog(int task, int rapid) {
        Set<String> lineSet = new HashSet<>();
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            String pathname = "D:\\Coding\\JavaProject\\multi-agent-v3\\data\\outputGA20-" + (task*20) + "-" + rapid + ".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
//            line = br.readLine();
            do {
                line = br.readLine(); // 一次读入一行数据
                if (line != null) {
                    lineSet.add(line);
                }
            } while (line != null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineSet;
    }

    public int totalCost(Set<String> lineSet, List<List<int[]>> processCost) {
        int total = 0;
        for (String line : lineSet) {
            System.out.println("line " + line);
            String[] strs = line.split(" ");
            int process = Integer.parseInt(strs[9]);
            int task = Integer.parseInt(strs[10])-1;
            int startTime = Integer.parseInt(strs[11]);
            int endTime = Integer.parseInt(strs[12]);
            int resource = Integer.parseInt(strs[13]);
            System.out.println(task + " " + process + " " + resource);
            total += processCost.get(task).
                    get(process)
                    [resource] * (endTime-startTime);
        }
        return total;
    }



    public static void main(String[] args) {
        ReadOutputGALog readLog = new ReadOutputGALog();


        Set<String> lineSet = readLog.readGALog(1, 1);
        List<List<int[]>> processCost = readLog.getProcessCost(1,1,20,20);

        for (int i=0; i<processCost.size(); i++) {
            System.out.println(i);
            for (int j=0; j<processCost.get(i).size(); j++) {

                for (int k=0; k<processCost.get(i).get(j).length; k++) {
                    System.out.print(processCost.get(i).get(j)[k] + " ");
                }
                System.out.println();
            }
            System.out.println();
        }

        System.out.println(readLog.totalCost(lineSet, processCost));
        for (String line : lineSet) {
            System.out.println(line);
        }
        System.out.println(lineSet.size());
    }

}
