package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/21 16:31
 * @description：exchange 成交
 */
@Data
public class DealExchange implements BasicCommon {
    private Integer preStartTime;
    private Integer preEndTime;

    private Integer startTime;
    private Integer endTime;
}
