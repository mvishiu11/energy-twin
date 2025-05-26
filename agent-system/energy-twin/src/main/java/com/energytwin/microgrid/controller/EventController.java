package com.energytwin.microgrid.controller;

import com.energytwin.microgrid.service.EventControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventControlService eventControlService;

    @PostMapping("/breakPanel")
    public ResponseEntity<String> breakPanel(
        @RequestParam String name,
        @RequestParam(required = false, defaultValue = "10") int ticks
    ){
        if( ticks < 10){
            return ResponseEntity.badRequest().body("Ticks must be at least 10");
        }

        eventControlService.addBrokenPanel(name, ticks);

        return ResponseEntity.ok("Simulation resumed.");
    }

}
