package com.dragun.ecommerce.integration.mail;

import lombok.extern.slf4j.Slf4j;

/** Used when mail is disabled or not configured (local dev, tests): logs instead of calling Brevo. */
@Slf4j
public class LoggingMailSender implements MailSender {

    @Override
    public boolean send(MailMessage message) {
        log.info("Mail disabled, not sent: subject='{}' to={}", message.subject(), message.maskedRecipients());
        return false;
    }
}
