package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 20:22
 * @description：反馈消息
 */
@Data
public class ProposeMsg implements BasicCommon {
    private List<ProcessTime> waitProcessTimes;
    private String resourceName;
    private int no;
    private String taskName;

}
