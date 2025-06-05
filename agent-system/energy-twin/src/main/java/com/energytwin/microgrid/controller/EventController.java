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

    /**
     * Breaks the component of the system - panel or Battery ONLY
     * @param name unique name of the component to break
     * @param ticks number of ticks indicating how long the component will be broken ( default is 10 )
     * @return HTTP response indicating success or error
     */
    @PostMapping("/breakComponent")
    public ResponseEntity<String> breakSource(
        @RequestParam String name,
        @RequestParam(required = false, defaultValue = "8") int ticks
    ){
        if( ticks < 1){
            return ResponseEntity.badRequest().body("Ticks must be at least 1");
        }

        eventControlService.addBrokenComponent(name, ticks);

        return ResponseEntity.ok("Component broken.");
    }

    /**
     * Sudden energy spike for Load Agents
     * @param name unique name of the load agent
     * @param ticks number of ticks indicating how long the energy consumption will be higher ( default is 10 )
     * @param rate rate which indicates energy consumption increase ( default is 2 )
     * @return HTTP response indicating success or error
     */

    @PostMapping("/loadSpike")
    public ResponseEntity<String> loadSpike(
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "8") int ticks,
            @RequestParam(required = false, defaultValue = "2") int rate
    ){
        if( ticks < 1){
            return ResponseEntity.badRequest().body("Ticks must be at least 1");
        }

        eventControlService.addLoadSpike(name, ticks, rate);

        return ResponseEntity.ok("Load spike added.");
    }

}
