package com.energytwin.microgrid.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Service
public class EventControlService {

    private final Map<String,Integer> brokenPanels = new ConcurrentHashMap<>();

    public void addBrokenPanel(String name, int ticks){
        brokenPanels.put(name, ticks);
        System.out.println("Panel " + name + " broken for: " + ticks);
    }

    public boolean isBroken(String name) {
        Integer remaining = brokenPanels.get(name);
        if (remaining == null || remaining < 1) {
            return false;
        }
        remaining--;
        if (remaining == 0) {
            brokenPanels.remove(name);
        } else {
            brokenPanels.put(name, remaining);
        }
        System.out.println("Panel: " + name + " ticks remaining: " + remaining);
        return true;
    }


}
