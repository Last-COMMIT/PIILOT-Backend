package com.lastcommit.piilot;

import com.lastcommit.piilot.global.config.AiServerProperties;
import com.lastcommit.piilot.global.config.JwtProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@SpringBootApplication
@EnableJpaRepositories
@EnableJpaAuditing
@EnableConfigurationProperties({JwtProperties.class, AiServerProperties.class})
public class PiilotApplication {

    @PostConstruct
    public void started(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

	public static void main(String[] args) {
		SpringApplication.run(PiilotApplication.class, args);
	}

}
