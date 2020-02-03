package com.inspire.development;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevelopmentApplication {
  private static final Logger LOGGER = LogManager.getLogger(DevelopmentApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(DevelopmentApplication.class, args);
  }
}
