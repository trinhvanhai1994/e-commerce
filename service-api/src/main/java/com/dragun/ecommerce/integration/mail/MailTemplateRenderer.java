package com.dragun.ecommerce.integration.mail;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

/** Renders {@code classpath:mail/<name>.html}. Templates use th:text, so every value is HTML-escaped. */
@Component
public class MailTemplateRenderer {

    private static final Locale VI = Locale.forLanguageTag("vi");

    private final TemplateEngine engine;

    public MailTemplateRenderer() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("mail/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);
        engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
    }

    public String render(String template, Map<String, Object> variables) {
        return engine.process(template, new Context(VI, variables));
    }
}
