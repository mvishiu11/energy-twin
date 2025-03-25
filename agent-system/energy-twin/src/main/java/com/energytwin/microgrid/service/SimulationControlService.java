package com.energytwin.microgrid.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

/** Manages simulation control parameters. */
@Getter
@Service
public class SimulationControlService {

  @Setter private int tickIntervalMillis = 1000;
  @Setter private double speedUpFactor = 1;
  private volatile boolean paused = false;

  /**
   * Returns the simulation delay.
   *
   * @return simulation delay.
   */
  public long getSimulationDelay() {
    return (long) (tickIntervalMillis / speedUpFactor);
  }

  public void pause() {
    this.paused = true;
  }

  public void resume() {
    this.paused = false;
  }
}
