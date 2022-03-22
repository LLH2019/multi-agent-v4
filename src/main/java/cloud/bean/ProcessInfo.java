package cloud.bean;

import jnr.ffi.annotations.In;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 20:10
 * @description：加工时间
 */
@Data
public class ProcessInfo {
    private Integer startTime;
    private Integer endTime;
    private String taskName;
    private Integer no;
    private String resourceName;
    private Integer preTime;
    private String taskProcessNum;
//    private String from

    public ProcessInfo() {
    }

    public ProcessInfo(Integer startTime, Integer endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
