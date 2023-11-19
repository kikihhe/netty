package com.xiaohe.mailtest.alarm;

import com.xiaohe.mailtest.MailMessage;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-14 12:42
 */
public interface MailAlarm {
    public void sendMail(MailMessage mailMessage);
}
