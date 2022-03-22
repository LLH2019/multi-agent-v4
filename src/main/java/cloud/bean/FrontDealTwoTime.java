package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/21 16:31
 * @description：exchange 成交
 */
@Data
public class FrontDealTwoTime implements BasicCommon {
    private Integer preStartTime;
    private Integer preEndTime;

    private Integer startTime;
    private Integer endTime;
    private List<ProcessInfo> processInfos;
    private String zeroResource;
    private String oneResource;
    private String twoResource;
}
