package com.energytwin.microgrid.agentfusion.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Holds a static reference to the Spring ApplicationContext so that JADE agents (which are not
 * managed by Spring) can retrieve Spring beans.
 */
@Component
public class SpringContext implements ApplicationContextAware {
  private static ApplicationContext context;

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    SpringContext.context = ctx;
  }

  public static ApplicationContext getApplicationContext() {
    return context;
  }
}
