package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/22 19:16
 * @description：询问最后的resource信息
 */
@Data
public class ResponseFinalResourceInfo implements BasicCommon {
    private String resourceName;
    private List<ProcessInfo> haveAssignedProcessInfos;
}
