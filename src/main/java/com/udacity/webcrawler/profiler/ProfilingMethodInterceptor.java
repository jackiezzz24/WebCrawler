package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object profiledObj;
  private final ProfilingState state;
  private final ZonedDateTime startTime;

  ProfilingMethodInterceptor(Clock clock, Object profiledObj, ProfilingState state, ZonedDateTime startTime) {
    this.clock = Objects.requireNonNull(clock);
    this.profiledObj = profiledObj;
    this.state = state;
    this.startTime = startTime;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    //       This method interceptor should inspect the called method to see if it is a profiled
    //       method. For profiled methods, the interceptor should record the start time, then
    //       invoke the method using the object that is being profiled. Finally, for profiled
    //       methods, the interceptor should record how long the method call took, using the
    //       ProfilingState methods.
    Object invoked;
    Instant start = null;
    boolean profiled = method.getAnnotation(Profiled.class) != null;
    if (profiled) {
      start = clock.instant();
    }
    try {
      invoked = method.invoke(profiledObj, args);
    } catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
      finally {
      if (profiled) {
        Duration duration = Duration.between(start, clock.instant());
        state.record(profiledObj.getClass(), method, duration);
      }
    }
    return invoked;
  }
}
