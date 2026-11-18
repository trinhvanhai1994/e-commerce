package com.dragun.ecommerce.integration.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@DisplayName("BrevoMailSender")
class BrevoMailSenderTest {

    private static final String URL = "https://brevo.test/v3/smtp/email";

    private MockRestServiceServer server;
    private BrevoMailSender sender;

    @BeforeEach
    void setUp() {
        MailProperties properties = new MailProperties();
        properties.setEnabled(true);
        properties.setFromEmail("no-reply@shop.test");
        properties.setFromName("Thi Yên");
        properties.getBrevo().setApiKey("test-api-key");
        properties.getBrevo().setBaseUrl("https://brevo.test");

        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        sender = new BrevoMailSender(builder, properties);
    }

    private static MailMessage message() {
        return new MailMessage(List.of("khach@example.com", "admin@example.com"), "Xác nhận đơn #1",
            "<p>Xin chào</p>", "Xin chào");
    }

    @Test
    @DisplayName("posts sender, recipients, subject, html and text with the API key; 201 counts as sent")
    void send_ok() {
        server.expect(requestTo(URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("api-key", "test-api-key"))
            .andExpect(jsonPath("$.sender.email").value("no-reply@shop.test"))
            .andExpect(jsonPath("$.sender.name").value("Thi Yên"))
            .andExpect(jsonPath("$.to[0].email").value("khach@example.com"))
            .andExpect(jsonPath("$.to[1].email").value("admin@example.com"))
            .andExpect(jsonPath("$.subject").value("Xác nhận đơn #1"))
            .andExpect(jsonPath("$.htmlContent").value("<p>Xin chào</p>"))
            .andExpect(jsonPath("$.textContent").value("Xin chào"))
            .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                .body("{\"messageId\":\"<abc@smtp-relay.brevo.com>\"}"));

        assertThat(sender.send(message())).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("a 4xx (e.g. wrong key) returns false instead of throwing")
    void send_rejected() {
        server.expect(requestTo(URL))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"unauthorized\",\"message\":\"Key not found\"}"));

        assertThat(sender.send(message())).isFalse();
        server.verify();
    }

    @Test
    @DisplayName("a 5xx returns false and is not retried (a retry could mail twice)")
    void send_serverError_noRetry() {
        server.expect(requestTo(URL)).andRespond(withServerError());

        assertThat(sender.send(message())).isFalse();
        server.verify();
    }

    @Test
    @DisplayName("mask keeps the domain and two characters of the local part")
    void mask() {
        assertThat(MailMessage.mask("khach@example.com")).isEqualTo("kh***@example.com");
        assertThat(MailMessage.mask("a@b.vn")).isEqualTo("a***@b.vn");
        assertThat(MailMessage.mask("not-an-email")).isEqualTo("***");
    }
}
