package com.arivubuilds.internals.solid.p06_dip.bad;

public class SmtpAlertClient {
    public String sendEmail(String toAddress, String subject) {
        return "SMTP_SENT_TO:" + toAddress + ":" + subject;
    }
}
