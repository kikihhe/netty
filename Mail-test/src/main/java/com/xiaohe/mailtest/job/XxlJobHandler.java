package com.xiaohe.mailtest.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-19 22:03
 */
@Component
public class XxlJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobHandler.class);
    @XxlJob("checkStore")
    public void check() throws InterruptedException {
        String jobParam = XxlJobHelper.getJobParam();
        System.out.println("定时任务的参数: " + jobParam);

        Thread.sleep(5 * 1000);

        logger.info("开始执行定时任务");
        int store = new Random().nextInt(3);
        XxlJobHelper.log("检查库存，库存余量: {}", store);
        if (store <= 0) {
            System.out.println("东西卖的太快了，快添加库存");
        }
        logger.info("定时任务执行完毕");
    }


}
