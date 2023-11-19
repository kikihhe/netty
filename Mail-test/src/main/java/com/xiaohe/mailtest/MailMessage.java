package com.xiaohe.mailtest;

/**
 * @author : 小何
 * @Description :
 * @date : 2023-11-14 12:34
 */

public class MailMessage {
    /**
     * 收件人
     */
    private String[] tos;
    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    public MailMessage() {
    }

    public MailMessage(String[] tos, String subject, String content) {
        this.tos = tos;
        this.subject = subject;
        this.content = content;
    }

    public static class MailMessageBuild {
        public static MailMessage buildVerificationMail(String[] tos, String code) {
            MailMessage mailMessage = new MailMessage();
            mailMessage.setTos(tos);
            mailMessage.setSubject("验证码");
            mailMessage.setContent("您的验证码为" + code);
            return mailMessage;
        }
    }


    public String[] getTos() {
        return tos;
    }

    public void setTos(String[] tos) {
        this.tos = tos;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
