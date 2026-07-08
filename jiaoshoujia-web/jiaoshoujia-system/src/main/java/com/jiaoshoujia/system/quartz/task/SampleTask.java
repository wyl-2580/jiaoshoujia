package com.jiaoshoujia.system.quartz.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 示例定时任务。在"调用方法"中填写 sampleTask.run 或 sampleTask.runWithParam('hello') 即可调度。
 */
@Component("sampleTask")
public class SampleTask {

    private static final Logger log = LoggerFactory.getLogger(SampleTask.class);

    public void run() {
        log.info("示例定时任务执行 - 无参");
    }

    public void runWithParam(String param) {
        log.info("示例定时任务执行 - 参数: {}", param);
    }
}
