package com.dragun.ecommerce.integration.meinvoice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MeinvoiceWebClientConfig {

  private static final int MAX_IN_MEMORY_BYTES = 16 * 1024 * 1024;

    @Bean(name = "meinvoiceWebClient")
    public WebClient meinvoiceWebClient(MeinvoiceIntegrationConfig config) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecConfigurer -> {
                    ClientCodecConfigurer.ClientDefaultCodecs codecs = codecConfigurer.defaultCodecs();
                    codecs.maxInMemorySize(MAX_IN_MEMORY_BYTES);
                })
                .build();
        return WebClient.builder()
                .baseUrl(config.getApi().getBaseUrl())
                .exchangeStrategies(strategies)
                .build();
    }
}
