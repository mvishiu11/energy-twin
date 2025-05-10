package com.energytwin.microgrid.ws.service;

import com.energytwin.microgrid.ws.dto.MetricsMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class MetricsPublishingService {
    private final SimpMessagingTemplate template;

    public MetricsPublishingService(SimpMessagingTemplate template){
        this.template = template;
    }

    public void publish(MetricsMessage message){
        template.convertAndSend("/topic/metrics", message);
    }

}
