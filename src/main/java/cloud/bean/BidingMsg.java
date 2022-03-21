package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 19:58
 * @description：招投标信息
 */

@Data
public class BidingMsg implements BasicCommon {
    private String sender;
    private String task;
    private String content;
    private Integer startTime;
    private int no;

}
