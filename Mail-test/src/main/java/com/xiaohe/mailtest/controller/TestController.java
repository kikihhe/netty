package com.xiaohe.mailtest.controller;

import com.xiaohe.mailtest.MailMessage;
import com.xiaohe.mailtest.alarm.MailAlarm;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-14 17:49
 */
@RestController
public class TestController {
    @Resource
    private MailAlarm mailAlarm;

    @RequestMapping("sendMail")
    public String send() {
        String[] to = new String[]{"2382546457@qq.com"};
        MailMessage mailMessage = MailMessage.MailMessageBuild.buildVerificationMail(to, "123456");
        mailAlarm.sendMail(mailMessage);
        return "发送成功";
    }
}
