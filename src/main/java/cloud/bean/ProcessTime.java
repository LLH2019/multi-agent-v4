package cloud.bean;

import jnr.ffi.annotations.In;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 20:10
 * @description：加工时间
 */
@Data
public class ProcessTime {
    private Integer startTime;
    private Integer endTime;

    private String resourceName;

    public ProcessTime() {
    }

    public ProcessTime(Integer startTime, Integer endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
