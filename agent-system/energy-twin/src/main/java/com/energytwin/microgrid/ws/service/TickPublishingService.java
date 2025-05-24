package com.energytwin.microgrid.ws.service;

import com.energytwin.microgrid.ws.dto.TickDataMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class TickPublishingService {
    private final SimpMessagingTemplate template;

    public TickPublishingService(SimpMessagingTemplate template){
        this.template = template;
    }

    public void publish(TickDataMessage message){
        template.convertAndSend("/topic/tickData", message);
    }
}
