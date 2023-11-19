package com.xiaohe.mailtest.alarm.impl;

import com.xiaohe.mailtest.MailMessage;
import com.xiaohe.mailtest.alarm.MailAlarm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;


/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-14 12:43
 */
@Component
public class QQMailAlarm implements MailAlarm {
    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendMail(MailMessage mailMessage) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(from);
        simpleMailMessage.setTo(mailMessage.getTos());
        simpleMailMessage.setSubject(mailMessage.getSubject());
        simpleMailMessage.setText(mailMessage.getContent());
        try {
            mailSender.send(simpleMailMessage);

        } catch (Exception e) {
            System.out.println("发送失败");
        }
    }
}
