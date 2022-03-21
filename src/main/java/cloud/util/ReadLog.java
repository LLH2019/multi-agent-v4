package cloud.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 8:53
 * @description：读取日志
 */
public class ReadLog {
    public void readLog() {
        int[][] fitness = new int[11][11];
        List<Integer> times = new ArrayList<>();
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw


            /* 读入TXT文件 */
            String pathname = "D:\\Coding\\JavaProject\\multi-agent-v3\\info.log"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
            File filename = new File(pathname); // 要读取以上路径的input。txt文件
            InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(filename)); // 建立一个输入流对象reader
            BufferedReader br = new BufferedReader(reader); // 建立一个对象，它把文件内容转成计算机能读懂的语言
            String line = "";
            String preLine = "";
            String nextLine = "";
//            line = br.readLine();
            do {
                line = br.readLine(); // 一次读入一行数据
                if (line != null) {
                    String[] strs = line.split(" ");

                    if ("task".equals(strs[6])) {
                        nextLine = br.readLine();
                        String[] nextStrs = nextLine.split(" ");
                        int task = Integer.parseInt(strs[7]);
                        int rapid = Integer.parseInt(nextStrs[7]);

                        String[] preStrs = preLine.split(" ");
                        if (preStrs.length == 8) {
                            fitness[task][rapid] = Integer.parseInt(preStrs[7]);
                            times.add(Integer.parseInt(preStrs[7]));
                        }

                    }
//                    System.out.println(strs.length);

//                    maxNum = Math.max(maxNum, Integer.parseInt(strs[4]));
//                    if (!resourceMaps.containsKey(strs[5])) {
//                        List<int[]> timeGaps = new ArrayList<>();
//                        timeGaps.add(new int[]{Integer.parseInt(strs[3]), Integer.parseInt(strs[4])});
//                        resourceMaps.put(strs[5], timeGaps);
//                    } else {
//                        List<int[]> timeGaps = resourceMaps.get(strs[5]);
//                        timeGaps.add(new int[]{Integer.parseInt(strs[3]), Integer.parseInt(strs[4])});
//                    }
//                    System.out.println(line);
                }
                preLine = line;
            } while (line != null);
        } catch (Exception e) {
            e.printStackTrace();
        }


        for (int i=1; i<=10; i++) {
            for (int j=1; j<=10; j++) {
                System.out.print(fitness[i][j] + " ");
            }
            System.out.println();
        }
        StringBuffer sb = new StringBuffer();
        for(int i=0; i<times.size(); i++) {
            sb.append(times.get(i));
            sb.append(",");
        }
        System.out.println(sb.toString());
        System.out.println(times.size());
    }

    public static void main(String[] args) {
        ReadLog readLog = new ReadLog();
        readLog.readLog();
    }

}
