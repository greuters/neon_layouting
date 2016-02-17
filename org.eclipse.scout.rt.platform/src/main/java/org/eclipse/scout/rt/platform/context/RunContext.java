/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.context;

import java.security.AccessController;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.nls.NlsLocale;
import org.eclipse.scout.rt.platform.security.SubjectProcessor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.platform.util.ThreadLocalProcessor;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.Callables;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * A context typically represents a "snapshot" of the current calling state and is always associated with a
 * {@link RunMonitor}. This class facilitates propagation of that state among different threads, or allows temporary
 * state changes to be done for the time of executing some code.
 * <p>
 * Internally, the context is obtained by <code>BEANS.get(RunContext.class)</code>, meaning that the context can be
 * intercepted, or replaced. Thereto, the method {@link #interceptCallableChain(CallableChain)} can be overwritten to
 * contribute some additional behavior.
 *
 * @since 5.1
 */
@Bean
public class RunContext implements IAdaptable {

  protected RunMonitor m_runMonitor = BEANS.get(RunMonitor.class);
  protected Subject m_subject;
  protected Locale m_locale;
  protected PropertyMap m_propertyMap = new PropertyMap();
  protected Deque<String> m_identifiers = new LinkedList<>();

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}. Use this method if you run code that does
   * not return a result.
   *
   * @param runnable
   *          runnable to be run.
   * @throws RuntimeException
   *           if the runnable throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  public void run(final IRunnable runnable) {
    call(Callables.callable(runnable));
  }

  /**
   * Runs the given {@link IRunnable} on behalf of this {@link RunContext}, and allows translation of exceptions thrown
   * during execution.
   *
   * @param runnable
   *          runnable to be run.
   * @param exceptionTranslator
   *          to translate exceptions thrown during execution.
   * @throws EXCEPTION
   *           if the runnable throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  public <EXCEPTION extends Throwable> void run(final IRunnable runnable, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    call(Callables.callable(runnable), exceptionTranslator);
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}. Use this method if you run code that returns
   * a result.
   *
   * @param callable
   *          callable to be run.
   * @return the return value of the callable.
   * @throws RuntimeException
   *           if the callable throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  public <RESULT> RESULT call(final Callable<RESULT> callable) {
    return call(callable, DefaultRuntimeExceptionTranslator.class);
  }

  /**
   * Runs the given {@link Callable} on behalf of this {@link RunContext}, and allows translation of exceptions thrown
   * during execution.
   *
   * @param callable
   *          callable to be run.
   * @param exceptionTranslator
   *          to translate exceptions thrown during execution.
   * @return the return value of the callable.
   * @throws EXCEPTION
   *           if the callable throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  public <RESULT, EXCEPTION extends Throwable> RESULT call(final Callable<RESULT> callable, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    final ThreadInterrupter threadInterrupter = new ThreadInterrupter(Thread.currentThread(), m_runMonitor);
    try {
      final CallableChain<RESULT> callableChain = new CallableChain<>();
      interceptCallableChain(callableChain);
      return callableChain.call(callable);
    }
    catch (final Throwable t) {
      throw BEANS.get(exceptionTranslator).translate(t);
    }
    finally {
      threadInterrupter.destroy();
    }
  }

  /**
   * Method invoked to contribute to the {@link CallableChain} to initialize this context. Overwrite this method to
   * contribute some behavior to the context.
   * <p>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility'.<br>
   * To contribute to the end of the chain (meaning that you are invoked <strong>after</strong> the contributions of
   * super classes and therefore can base on their contributed functionality), you can use constructions of the
   * following form:
   *
   * <pre>
   * super.interceptCallableChain(callableChain);
   * callableChain.addLast(new YourDecorator());
   * </pre>
   *
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   *
   * <pre>
   * super.interceptCallableChain(callableChain);
   * callableChain.addFirst(new YourDecorator());
   * </pre>
   *
   * @param callableChain
   *          The chain used to construct the context.
   */
  protected <RESULT> void interceptCallableChain(final CallableChain<RESULT> callableChain) {
    callableChain
        .add(new ThreadLocalProcessor<>(RunMonitor.CURRENT, Assertions.assertNotNull(m_runMonitor)))
        .add(new SubjectProcessor<RESULT>(m_subject))
        .add(new DiagnosticContextValueProcessor(BEANS.get(PrinicpalContextValueProvider.class)))
        .add(new ThreadLocalProcessor<>(NlsLocale.CURRENT, m_locale))
        .add(new ThreadLocalProcessor<>(PropertyMap.CURRENT, m_propertyMap))
        .add(new ThreadLocalProcessor<>(RunContextIdentifiers.CURRENT, m_identifiers));
  }

  /**
   * @return {@link RunMonitor} to be used, is never <code>null</code>.
   */
  public RunMonitor getRunMonitor() {
    return m_runMonitor;
  }

  /**
   * Set a specific {@link RunMonitor} to be used, which must not be <code>null</code>. However, even if there is a
   * current {@link RunMonitor}, it is NOT registered as child monitor, meaning that it will not be cancelled once the
   * current {@link RunMonitor} is cancelled. If such a linking is needed, you have to do that yourself:
   *
   * <pre>
   * <code>
   *     RunMonitor monitor = BEANS.get(RunMonitor.class);
   *
   *     // Register your monitor to be cancelled as well
   *     RunMonitor.CURRENT.get().registerCancellable(monitor);
   *
   *     RunContexts.copyCurrent().withRunMonitor(monitor).run(new IRunnable() {
   *
   *       &#064;Override
   *       public void run() throws Exception {
   *         // do something
   *       }
   *     });
   * </code>
   * </pre>
   */
  public RunContext withRunMonitor(final RunMonitor runMonitor) {
    m_runMonitor = Assertions.assertNotNull(runMonitor, "RunMonitor must not be null");
    return this;
  }

  /**
   * @see #withSubject(Subject)
   */
  public Subject getSubject() {
    return m_subject;
  }

  /**
   * Associates this context with the given {@link Subject}, meaning that any code running on behalf of this context is
   * run as the given {@link Subject}.
   */
  public RunContext withSubject(final Subject subject) {
    m_subject = subject;
    return this;
  }

  /**
   * @see #withLocale(Locale)
   */
  public Locale getLocale() {
    return m_locale;
  }

  /**
   * Associates this context with the given {@link Locale}, meaning that any code running on behalf of this context has
   * that {@link Locale} set in {@link NlsLocale#CURRENT} thread-local.
   */
  public RunContext withLocale(final Locale locale) {
    m_locale = locale;
    return this;
  }

  /**
   * Returns the {@link PropertyMap} associated with this context.
   *
   * @see #withProperty(Object, Object)
   */
  public PropertyMap getPropertyMap() {
    return m_propertyMap;
  }

  /**
   * Returns the property value to which the specified key is mapped, or <code>null</code> if not associated with this
   * context.
   *
   * @see #withProperty(Object, Object)
   */
  public <VALUE> VALUE getProperty(final Object key) {
    return m_propertyMap.get(key);
  }

  /**
   * Returns the property value to which the specified key is mapped, or {@code defaultValue} if not associated with
   * this context.
   *
   * @see #withProperty(Object, Object)
   */
  public <VALUE> VALUE getPropertyOrDefault(final Object key, final VALUE defaultValue) {
    return m_propertyMap.getOrDefault(key, defaultValue);
  }

  /**
   * Returns whether the given property is associated with this context.
   *
   * @see #withProperty(Object, Object)
   */
  public boolean containsProperty(final Object key) {
    return m_propertyMap.contains(key);
  }

  /**
   * Associates this context with the given 'key-value' property, meaning that any code running on behalf of this
   * context has that property set in {@link PropertyMap#CURRENT} thread-local.
   * <p>
   * To remove a property, use <code>null</code> as its value.
   */
  public RunContext withProperty(final Object key, final Object value) {
    m_propertyMap.put(key, value);
    return this;
  }

  /**
   * Associates this context with the given 'key-value' properties, meaning that any code running on behalf of this
   * context has those properties set in {@link PropertyMap#CURRENT} thread-local.
   */
  public RunContext withProperties(final Map<?, ?> properties) {
    for (final Entry<?, ?> propertyEntry : properties.entrySet()) {
      withProperty(propertyEntry.getKey(), propertyEntry.getValue());
    }
    return this;
  }

  /**
   * Gets a live reference to the identifiers of this run context.
   *
   * @return A {@link Deque} with all identifiers of this context having the current identifier on top of the deque.
   * @see RunContextIdentifiers#isCurrent(String)
   */
  public Deque<String> getIdentifiers() {
    return m_identifiers;
  }

  /**
   * Pushes a new identifier on top of the identifiers {@link Deque}.
   *
   * @param id
   *          The new top identifier.
   * @return this
   * @see RunContextIdentifiers#isCurrent(String)
   */
  public RunContext withIdentifier(final String id) {
    m_identifiers.push(id);
    return this;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.ref("runMonitor", getRunMonitor());
    builder.ref("subject", getSubject());
    builder.attr("locale", getLocale());
    builder.attr("identifiers", CollectionUtility.format(getIdentifiers()));
    return builder.toString();
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the given {@link RunContext}.
   */
  protected void copyValues(final RunContext origin) {
    m_runMonitor = origin.m_runMonitor;
    m_subject = origin.m_subject;
    m_locale = origin.m_locale;
    m_propertyMap = new PropertyMap(origin.m_propertyMap);
    m_identifiers = new LinkedList<>(origin.m_identifiers);
  }

  /**
   * Method invoked to fill this {@link RunContext} with values from the current calling {@link RunContext}.
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created, and if the current calling context contains a {@link RunMonitor}, it is also
   * registered within that {@link RunMonitor}. That makes the <i>returned</i> {@link RunContext} to be cancelled as
   * well once the current calling {@link RunContext} is cancelled, but DOES NOT cancel the current calling
   * {@link RunContext} if the <i>returned</i> {@link RunContext} is cancelled.
   */
  protected void fillCurrentValues() {
    m_subject = Subject.getSubject(AccessController.getContext());
    m_locale = NlsLocale.CURRENT.get();
    m_propertyMap = new PropertyMap(PropertyMap.CURRENT.get());

    // RunMonitor
    m_runMonitor = BEANS.get(RunMonitor.class);
    if (RunMonitor.CURRENT.get() != null) {
      RunMonitor.CURRENT.get().registerCancellable(m_runMonitor);
    }

    // RunContextIdentifiers
    m_identifiers = new LinkedList<>();
    final Deque<String> callingRunContextIdentifiers = RunContextIdentifiers.CURRENT.get();
    if (callingRunContextIdentifiers != null) {
      m_identifiers.addAll(callingRunContextIdentifiers);
    }
  }

  /**
   * Method invoked to fill this {@link RunContext} with empty values.
   * <p>
   * <strong>RunMonitor</strong><br>
   * a new {@link RunMonitor} is created. However, even if there is a current {@link RunMonitor}, it is NOT registered
   * as child monitor, meaning that it will not be cancelled once the current {@link RunMonitor} is cancelled.
   */
  protected void fillEmptyValues() {
    m_subject = null;
    m_locale = null;
    m_runMonitor = BEANS.get(RunMonitor.class);
    m_propertyMap = new PropertyMap();
    m_identifiers = new LinkedList<>();
  }

  /**
   * Creates a copy of <code>this RunContext</code>.
   */
  public RunContext copy() {
    final RunContext copy = BEANS.get(RunContext.class);
    copy.copyValues(this);
    return copy;
  }

  @Override
  public <T> T getAdapter(final Class<T> type) {
    return null;
  }

  /**
   * Interrupts the associated thread upon a hard cancellation of the given {@link RunMonitor}.
   */
  private static class ThreadInterrupter implements ICancellable {

    private final RunMonitor m_monitor;
    private final AtomicBoolean m_cancelled = new AtomicBoolean();

    private volatile Thread m_thread;

    public ThreadInterrupter(final Thread thread, final RunMonitor monitor) {
      m_thread = thread;
      m_monitor = monitor;
      m_monitor.registerCancellable(this);
    }

    @Override
    public boolean isCancelled() {
      return m_cancelled.get();
    }

    @Override
    public boolean cancel(final boolean interruptIfRunning) {
      if (!m_cancelled.compareAndSet(false, true)) {
        return false;
      }

      if (interruptIfRunning) {
        synchronized (this) {
          // Interrupt in synchronized block to ensure the thread still to be associated upon interruption.
          if (m_thread != null) {
            m_thread.interrupt();
          }
        }
      }

      return true;
    }

    /**
     * Invoke to no longer interrupt the associated thread upon a hard cancellation of the monitor.
     */
    public synchronized void destroy() {
      m_monitor.unregisterCancellable(this);

      synchronized (this) {
        m_thread = null;
      }
    }
  }
}
