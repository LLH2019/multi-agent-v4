package cloud.bean;

import base.model.bean.BasicCommon;
import lombok.Data;

import java.util.List;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/21 16:17
 * @description：开始重复招标
 */
@Data
public class RepeatBiding implements BasicCommon {
    private String resourceName;
    private List<ProcessInfo> processInfos;
}
