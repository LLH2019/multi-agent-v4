package cloud.util;

import cloud.global.GlobalAkkaPara;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 14:21
 * @description：生成带成本的数据
 */
public class GenerationDataWithCost {


    public List<List<int[]>> readRelationData(int taskNum, int rapid, int resourceSize, int taskSize) {
        List<List<int[]>> taskAndResourceData = new ArrayList<>();
        Random random = new Random();
        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw
            /* 读入TXT文件 */
            String pathname = "D:\\Coding\\JavaProject\\multi-agent-v3\\data\\write" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
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
                        taskAndResourceData.add(processDataList);
                    } else {
                        String [] strs = line.split(",");
                        int [] timeAndCosts = new int[resourceSize*2];
                        for (int i=0; i<resourceSize; i++) {
                            timeAndCosts[i] = Integer.parseInt(strs[i]);
                        }
                        for (int k=resourceSize; k<2*resourceSize; k++) {
                            timeAndCosts[k] = random.nextInt(20)+10;
                        }
                        taskAndResourceData.get(taskAndResourceData.size()-1).add(timeAndCosts);
                    }
                }
            } while ( line != null);
            br.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return taskAndResourceData;
    }


    public static void main(String[] args) {

        GenerationDataWithCost generationDataWithCost = new GenerationDataWithCost();

        for(int taskNum=1; taskNum<=10; taskNum++) {
            for (int rapid=1; rapid<=10; rapid++) {

                int taskSize = 20;
                int resourceSize = 20;

                List<List<int[]>> datas = generationDataWithCost.readRelationData(taskNum, rapid, resourceSize,taskSize);

                try {
                    File writeName = new File("D:\\Coding\\JavaProject\\multi-agent-v3\\data\\write-with-cost-by" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
                    if (!writeName.exists()) {
                        writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
                    }
                    BufferedWriter out = new BufferedWriter(new FileWriter(writeName));
                    for (int i = 0; i < datas.size(); i++) {
                        out.write(String.valueOf(datas.get(i).size()));
                        out.newLine();
                        for (int j = 0; j < datas.get(i).size(); j++) {
                            StringBuffer sb = new StringBuffer();
                            for (int k = 0; k < datas.get(i).get(j).length; k++) {
                                sb.append(datas.get(i).get(j)[k]);
                                sb.append(',');
                            }

                            sb.deleteCharAt(sb.length() - 1);
                            out.write(sb.toString());
                            out.newLine();
                        }
                    }
                    out.flush(); // 把缓存区内容压入文件
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
