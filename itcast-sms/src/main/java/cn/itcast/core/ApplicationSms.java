package cn.itcast.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationSms {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ApplicationSms.class);
        application.run(args);
    }
}
