package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 21:51
 * @description：成交msg
 */

@Data
public class DealMsg implements BasicCommon {
    private ProcessInfo proposeProcessInfo;
    private String resourceName;
    private int no;
    private String taskName;
    private String taskProcessNum;

//    private Integer startTime;
//    private Integer endTime;
//    private Integer preTime;
//    private String task;
}
