package com.energytwin.microgrid.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Service
public class EventControlService {

    @Setter
    @Getter
    class IntPair{
        int rate;
        int ticks;

        public IntPair(int rate, int ticks) {
            this.rate = rate;
            this.ticks = ticks;
        }
    }

    private final Map<String,Integer> brokenComponents = new ConcurrentHashMap<>();
    private final Map<String, IntPair> loadSpikes = new ConcurrentHashMap<>();
    private final AtomicLong blackoutTicksRemaining = new AtomicLong(0);

    public void addBrokenComponent(String name, int ticks){
        brokenComponents.put(name, ticks);
    }

    public boolean isBroken(String name) {
        Integer remaining = brokenComponents.get(name);
        if (remaining == null || remaining < 1) {
            return false;
        }
        remaining--;
        if (remaining == 0) {
            brokenComponents.remove(name);
        } else {
            brokenComponents.put(name, remaining);
        }
        return true;
    }

    public void addLoadSpike(String name, int ticks, int rate){
        loadSpikes.put(name, new IntPair(rate, ticks));
    }

    public int checkLoadSpike(String name){
        return loadSpikes.computeIfPresent(name, (k, p) -> {
            if (--p.ticks <= 0) {
                return null;
            }
            return p;
        }) != null
                ? loadSpikes.get(name).rate
                : 1;
    }

    public void startBlackout(long ticks) {
        blackoutTicksRemaining.set(ticks);
    }

    public boolean inBlackoutAndTick() {
        long rem = blackoutTicksRemaining.get();
        if (rem > 0) {
            blackoutTicksRemaining.decrementAndGet();
            return true;
        }
        return false;
    }

    public long getBlackoutRemaining() { return blackoutTicksRemaining.get(); }
}

