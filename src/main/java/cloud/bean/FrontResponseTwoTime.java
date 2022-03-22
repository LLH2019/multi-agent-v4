package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/21 19:33
 * @description：向前广播一次
 */
@Data
public class FrontResponseTwoTime implements BasicCommon {
    private String fromResourceName;
    private Boolean isSuccess;
    private ProcessInfo processInfo;
    private ProcessInfo preProcessInfo;
    private List<ProcessInfo> processInfos;
    private String resourceName;
    private String zeroResource;
    private String oneResource;
    private String twoResource;
}
