package cloud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 8:53
 * @description：读取日志
 */
public class ReadOutputGAData {

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
            String pathname = "D:\\Coding\\JavaProject\\multi-agent-v3\\data\\log\\outputGA" + task + "-" + rapid + ".log"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
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

//        List<>

        return lineSet;
    }

    public int totalCost(Set<String> lineSet, List<List<int[]>> processCost, int taskNum) {
        int total = 0;
        List<List<int[]>> taskList = new ArrayList<>();
        for (int i=0; i<taskNum; i++) {
            List<int[]> processList = new ArrayList<>();
            taskList.add(processList);
        }

        for (String line : lineSet) {
//            System.out.println("line " + line);
            String[] strs = line.split(" ");
            int num = Integer.parseInt(strs[9]);
            int task = Integer.parseInt(strs[10])-1;
            int startTime = Integer.parseInt(strs[11]);
            int endTime = Integer.parseInt(strs[12]);
            int resource = Integer.parseInt(strs[13]);

            int[] resourceTime = new int[]{startTime, endTime, resource};
            taskList.get(task).add(resourceTime);
//            System.out.println();
//            System.out.println(task + " " + num + " " + resource);

        }

        for (List<int[]> task : taskList) {
            Collections.sort(task, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    return o1[0]-o2[0];
                }
            });
        }

        for (int i=0; i<taskList.size(); i++) {
            for (int j=0; j<taskList.get(i).size(); j++) {
                total += processCost.get(i).
                        get(j)
                        [taskList.get(i).get(j)[2]] * (taskList.get(i).get(j)[1]-taskList.get(i).get(j)[0]);
            }
        }

        return total;
    }



    public static void main(String[] args) {
        ReadOutputGAData readLog = new ReadOutputGAData();

        List<Integer> list = new ArrayList<>();

        int taskSize = 20;
        int resourceSize = 20;
        for (int task = 1; task <= 8; task++) {
            System.out.println(task);
            for (int rapid = 1; rapid <= 10; rapid++) {
                Set<String> lineSet = readLog.readGALog(task, rapid);
                List<List<int[]>> processCost = readLog.getProcessCost(task, rapid, taskSize, resourceSize);

//        for (int i=0; i<processCost.size(); i++) {
//            System.out.println(i);
//            for (int j=0; j<processCost.get(i).size(); j++) {
//
//                for (int k=0; k<processCost.get(i).get(j).length; k++) {
//                    System.out.print(processCost.get(i).get(j)[k] + " ");
//                }
//                System.out.println();
//            }
//            System.out.println();
//        }

                int totalCost = readLog.totalCost(lineSet, processCost, task*taskSize);
                System.out.println(totalCost + " ");
                list.add(totalCost);
//        for (String line : lineSet) {
//            System.out.println(line);
//        }
//        System.out.println(lineSet.size());
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
