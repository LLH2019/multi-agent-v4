package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 22:29
 * @description：开始投标
 */
@Data
public class StartBiding implements BasicCommon {
    private Integer round;
    private Integer startTime;
}
