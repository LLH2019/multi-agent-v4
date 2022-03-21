package cloud.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/16 15:08
 * @description：仿真数据生成
 */
public class SimulationDataGenerator {
//    private int taskSize =10;
//    private int resourceSize = 20;

    public List<List<int[]>> generateData(int taskSize, int resourceSize) {
        List<List<int[]>> tasks = new ArrayList<>();
        Random random = new Random();

        for (int i=0; i<taskSize; i++) {
            List<int[]> curTask = new ArrayList<>();
            int processNum = random.nextInt(5)+3;

            for (int j=0; j<processNum; j++) {
                // processTime 数据0-resourceSize为处理时间， resourceSize-2*resourceSize为加工成本情况
                int[] processTime = new int[resourceSize*2];
                for (int k = 0; k < resourceSize; k++) {
                    processTime[k] = random.nextInt(50) + 4;
                    int randomN = random.nextInt(10);
                    if (randomN <= 1) {
                        processTime[j] = 9999;
                    }
                }

                for (int k=resourceSize; k<2*resourceSize; k++) {
                    processTime[k] = random.nextInt(20)+10;
                }

                curTask.add(processTime);
            }
            tasks.add(curTask);
        }

//        for (int k=0; k<total*0.1; k++) {
//            int i = random.nextInt(100);
//            int j = random.nextInt(10);
//
//            tasks.get(i)[j%tasks.get(i).length] = 9999;
//        }

        int [][] resourceDis = new int[resourceSize][resourceSize];
        for(int i=0; i<resourceSize; i++) {
            for (int j=0; j<i; j++) {
                if (i<=4 || (i<=9 && j>=5) || (i<=14 && j>=10) || (i<=19 && j>=15)) {
                    resourceDis[i][j] = random.nextInt(3);
                } else {
                    resourceDis[i][j] = random.nextInt(3)+3;
                }
            }
        }
        for (int i=0; i<resourceSize; i++) {
            for (int j=i+1; j<resourceSize; j++) {
                resourceDis[i][j] = resourceDis[j][i];
            }
        }

        printData(tasks,resourceSize);
        for (int i=0; i<resourceSize; i++) {
            for (int j=0; j<resourceSize; j++) {
                System.out.print(resourceDis[i][j] + " ");
            }
            System.out.println();
        }

        return tasks;
    }

    private void printData(List<List<int[]>> tasks, int resourceSize) {
        for (int i=0; i<tasks.size(); i++) {
            System.out.println( i + "  " + tasks.get(i).size());
            for (int j=0; j<tasks.get(i).size(); j++) {
                for (int k=0; k<2*resourceSize; k++) {
                    System.out.print(tasks.get(i).get(j)[k] + " ");
                }
                System.out.println();
            }

        }
    }


    public static void main(String[] args) {

        SimulationDataGenerator simulationDataGenerator = new SimulationDataGenerator();

        for(int taskNum=1; taskNum<=10; taskNum++) {
            for (int rapid=1; rapid<=10; rapid++) {

                int taskSize = 20;
                int resourceSize = 20;

                List<List<int[]>> datas = simulationDataGenerator.generateData(taskSize*taskNum, resourceSize);

                try {
                    File writeName = new File("D:\\Coding\\JavaProject\\multi-agent-v3\\data\\write-with-cost-and-trans" + resourceSize + "-" + (taskSize*taskNum) +"-" + rapid +".txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
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

//        //第一步：设置输出的文件路径
//        //如果该目录下不存在该文件，则文件会被创建到指定目录下。如果该目录有同名文件，那么该文件将被覆盖。
//        File writeFile = new File("D:\\Coding\\JavaProject\\multi-agent-v3\\data\\write100.csv");
//
//        try{
//            //第二步：通过BufferedReader类创建一个使用默认大小输出缓冲区的缓冲字符输出流
//            BufferedWriter writeText = new BufferedWriter(new FileWriter(writeFile));
//
//            //第三步：将文档的下一行数据赋值给lineData，并判断是否为空，若不为空则输出
//            for(int i=0;i<datas.size();i++){
////                writeText.newLine();    //换行
//                //调用write的方法将字符串写到流中
//                StringBuffer sb = new StringBuffer();
//                for (int j=0; j<datas.get(i).length; j++) {
//                    sb.append(datas.get(i)[j]);
//                    sb.append(',');
//                }
//                sb.deleteCharAt(sb.length()-1);
//                writeText.write(sb.toString());
//                writeText.newLine();
//            }
//            //使用缓冲区的刷新方法将数据刷到目的地中
//            writeText.flush();
//            //关闭缓冲区，缓冲区没有调用系统底层资源，真正调用底层资源的是FileWriter对象，缓冲区仅仅是一个提高效率的作用
//            //因此，此处的close()方法关闭的是被缓存的流对象
//            writeText.close();
//        }catch (FileNotFoundException e){
//            System.out.println("没有找到指定文件");
//        }catch (IOException e){
//            System.out.println("文件读写出错");
//        }
            }
        }
    }

}
