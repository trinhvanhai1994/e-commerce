package com.dragun.ecommerce.integration.mail;

/** Sends one mail. Never throws: returns false and logs when the mail could not be handed over. */
public interface MailSender {

    boolean send(MailMessage message);
}
