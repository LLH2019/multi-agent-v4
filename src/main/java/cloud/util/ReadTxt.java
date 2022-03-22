package cloud.util;


import cloud.global.GlobalAkkaPara;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/16 17:21
 * @description：读取TXT数据
 */
public class ReadTxt {
    public List<List<String>> readTxtData(int taskNum, int rapid) {
        List<List<String>> taskAndResourceData = new ArrayList<>();

        List<String> taskList = new ArrayList<>();
        List<String> resourceList = new ArrayList<>();
        List<String> costList = new ArrayList<>();


        List<StringBuffer> curProcessTime = new ArrayList<>();
        List<StringBuffer> curProcessCost = new ArrayList<>();

        int accProcessNum = 0;

        try { // 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw

            /* 读入TXT文件 */
            String pathname =GlobalAkkaPara.dataPath +"write-with-cost"+ GlobalAkkaPara.resourceSize + "-" + (GlobalAkkaPara.taskSize*taskNum) +"-" + rapid +".txt"; // 绝对路径或相对路径都可以，这里是绝对路径，写入文件时演示相对路径
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
                        StringBuffer task = new StringBuffer();
                        for (int i=0; i<processNum; i++) {
                            task.append(accProcessNum++);
                            task.append(',');
                        }
                        task.deleteCharAt(task.length()-1);
                        taskList.add(task.toString());
                    } else {
                        String [] strs = line.split(",");
                        if (curProcessTime.size() == 0) {
                            for (int i=0; i<strs.length/2; i++) {
                                curProcessTime.add(new StringBuffer());
                                curProcessCost.add(new StringBuffer());
                            }
                        }
                        for (int i=0; i<strs.length/2; i++) {
                            curProcessTime.get(i).append(strs[i]);
                            curProcessTime.get(i).append(',');
                        }
                        for (int i=0; i<strs.length/2; i++) {
                            curProcessCost.get(i).append(strs[strs.length/2+i]);
                            curProcessCost.get(i).append(',');
                        }

                    }
//                    System.out.println(line);
                }
            } while ( line != null);
            br.close();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        for (int i=0; i<curProcessTime.size(); i++) {
            resourceList.add(curProcessTime.get(i).deleteCharAt(curProcessTime.get(i).length()-1).toString());
            costList.add(curProcessCost.get(i).deleteCharAt(curProcessCost.get(i).length()-1).toString());
        }
        taskAndResourceData.add(taskList);
        taskAndResourceData.add(resourceList);
        taskAndResourceData.add(costList);

        printTaskAndResource(taskAndResourceData);

        return taskAndResourceData;
    }

    private void printTaskAndResource(List<List<String>> taskAndResourceData) {
        List<String> tasks = taskAndResourceData.get(0);
        System.out.println("task : ");
        for (int i=0; i<tasks.size(); i++) {
            System.out.println(tasks.get(i));
        }

        System.out.println("resource : ");
        List<String> resources = taskAndResourceData.get(1);
        for (int i=0; i<resources.size(); i++) {
            System.out.println(resources.get(i));
        }

        System.out.println("cost : ");
        List<String> costList = taskAndResourceData.get(2);
        for (int i=0; i<costList.size(); i++) {
            System.out.println(costList.get(i));
        }
    }

    public static void main(String[] args) {
        int taskNum = 10;
        int rapid =1;
        ReadTxt readTxt = new ReadTxt();
        readTxt.readTxtData(taskNum, rapid);
    }
}
