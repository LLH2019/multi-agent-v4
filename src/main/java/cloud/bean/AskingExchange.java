package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/21 15:29
 * @description：询问是否交换
 */
@Data
public class AskingExchange implements BasicCommon {
    private Integer startTime;
    private Integer endTime;
    private String taskName;
    private String resourceName;
    private Integer preTime;
    private String task;
    private Integer no;
}
