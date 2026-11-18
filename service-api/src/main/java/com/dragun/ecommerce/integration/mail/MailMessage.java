package com.dragun.ecommerce.integration.mail;

import java.util.List;

/** One mail to send. {@code text} is the plain-text part for clients that do not render HTML. */
public record MailMessage(List<String> to, String subject, String html, String text) {

    /** "an***@gmail.com": recognisable in logs without writing customer addresses out in full. */
    public static String mask(String email) {
        if (email == null) {
            return "null";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.substring(0, Math.min(2, at)) + "***" + email.substring(at);
    }

    public String maskedRecipients() {
        return to.stream().map(MailMessage::mask).toList().toString();
    }
}
