package com.penta;

import com.penta.service.DataCollectionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class PentaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PentaApplication.class, args);
    }

    @Bean
    public CommandLineRunner initializeData(DataCollectionService dataCollectionService) {
        return args -> {
            System.out.println("Checking champion data...");
            if (dataCollectionService.getDataCollectionStatus().getTotalChampions() == 0) {
                System.out.println("Initializing champion data...");
                dataCollectionService.initializeChampionData();
                System.out.println("Champion data initialized!");
            } else {
                System.out.println("Champion data already exists (" + 
                    dataCollectionService.getDataCollectionStatus().getTotalChampions() + 
                    " champions). Skipping initialization.");
            }
        };
    }
}