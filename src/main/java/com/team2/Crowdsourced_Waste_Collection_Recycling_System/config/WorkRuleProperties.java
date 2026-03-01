package com.team2.Crowdsourced_Waste_Collection_Recycling_System.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "workrule")
@Getter
@Setter
public class WorkRuleProperties {
    private int acceptTimeoutHours = 4;
    private int slaHours = 72;
    private int suspendThreshold = 3;
    private int workingStartHour = 7;
    private int workingEndHour = 17;
    private double reassignRadiusKm = 10.0;
}
