package com.dragun.ecommerce.integration.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/**
 * Mail wiring. {@code @EnableAsync} lets OrderMailListener run on Boot's applicationTaskExecutor,
 * so a slow or failing Brevo call never holds up the checkout request.
 */
@Slf4j
@Configuration
@EnableAsync
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    @Bean
    public MailSender mailSender(MailProperties properties) {
        if (!properties.canSend()) {
            log.warn("Mail sending is off (needs app.mail.enabled=true, app.mail.from-email and BREVO_API_KEY): "
                + "mails are only logged");
            return new LoggingMailSender();
        }
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(properties.getBrevo().getConnectTimeout())
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getBrevo().getReadTimeout());
        log.info("Mail via Brevo {} from {}", properties.getBrevo().getBaseUrl(), properties.getFromEmail());
        return new BrevoMailSender(RestClient.builder().requestFactory(requestFactory), properties);
    }
}
