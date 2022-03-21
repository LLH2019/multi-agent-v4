package cloud.util;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/19 9:12
 * @description：数组生成模拟
 */
public class ArraySimulation {

    public static void arraySimulation() {
        StringBuffer sb = new StringBuffer();
        for (int i=1; i<=10; i++) {
            for (int j=0; j<10; j++) {
                sb.append(i);
                sb.append(",");
            }
        }
        System.out.println(sb.toString());
    }

    public static void array() {


    }
}
