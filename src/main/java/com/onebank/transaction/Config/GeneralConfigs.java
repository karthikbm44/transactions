package com.onebank.transaction.Config;

import com.onebank.transaction.Utiliy.MyTextClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeneralConfigs {


    @Bean
    public MyTextClass myTextClass(){
        return new MyTextClass();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("http://localhost:9001/api/user") // Replace with the base URL of the target service
                .build();
    }
}
