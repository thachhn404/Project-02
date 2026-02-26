package com.team2.Crowdsourced_Waste_Collection_Recycling_System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CrowdsourcedWasteCollectionRecyclingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrowdsourcedWasteCollectionRecyclingSystemApplication.class, args);
	}

}
