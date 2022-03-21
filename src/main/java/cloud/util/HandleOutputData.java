package cloud.util;

import scala.Int;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/17 18:57
 * @description：处理输出数据
 */
public class HandleOutputData {
    public int handleOutputData(int taskNum, int rapid, int taskSize, int resourceSize) {
        TreeMap<String, List<int[]>> resourceMaps = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int i1 = Integer.parseInt(o1.substring(8));
                int i2 = Integer.parseInt(o2.substring(8));
                return i1-i2;
            }
        });

        int maxNum = 0;

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            String pathname ="D:\\Coding\\JavaProject\\multi-agent4\\data\\output" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
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
                    maxNum = Math.max(maxNum, Integer.parseInt(strs[4]));
                    if (!resourceMaps.containsKey(strs[5])) {
                        List<int[]> timeGaps = new ArrayList<>();
                        timeGaps.add(new int[]{Integer.parseInt(strs[3]), Integer.parseInt(strs[4])});
                        resourceMaps.put(strs[5], timeGaps);
                    } else {
                        List<int[]> timeGaps = resourceMaps.get(strs[5]);
                        timeGaps.add(new int[]{Integer.parseInt(strs[3]), Integer.parseInt(strs[4])});
                    }
//                    System.out.println(line);
                }
            } while ( line != null);
        }catch (Exception e){
            e.printStackTrace();
        }

        for (Map.Entry<String, List<int[]>> entry : resourceMaps.entrySet()) {
            List<int[]> timeGaps = entry.getValue();
            Collections.sort(timeGaps, new Comparator<int[]>() {
                @Override
                public int compare(int[] o1, int[] o2) {
                    return o1[0]-o2[0];
                }
            });

            System.out.print(entry.getKey() + "   ");
            for (int[] timeGap :timeGaps) {
                System.out.print(timeGap[0] + " " +timeGap[1] +"    ");
            }
            System.out.println();
        }
        System.out.println(maxNum);
        return maxNum;
    }

    public static void main(String[] args) {
        HandleOutputData handleOutputData = new HandleOutputData();

        Map<Integer, List<Integer>>  maxNumMaps =  new HashMap<>();
        for(int taskNum=1; taskNum<=10; taskNum++) {
            if (!maxNumMaps.containsKey(taskNum)) {
                maxNumMaps.put(taskNum, new ArrayList<>());
            }
            for (int rapid = 1; rapid <= 10; rapid++) {
                int taskSize = 20;
                int resourceSize = 20;
                int maxNum = handleOutputData.handleOutputData(taskNum, rapid, taskSize, resourceSize);

                List<Integer> maxNumList = maxNumMaps.get(taskNum);
                maxNumList.add(maxNum);
            }
        }

        StringBuffer sb = new StringBuffer();

        for (Map.Entry<Integer, List<Integer>> entry: maxNumMaps.entrySet()) {
            System.out.print(entry.getKey() + " ");
            for (int val : entry.getValue()) {
                System.out.print(val + " ");
                sb.append(val);
                sb.append(",");
            }
            System.out.println();
        }
        System.out.println(sb.toString());

    }

}
