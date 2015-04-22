/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.BeanUtility;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.IBeanScopeEvaluator;
import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.SimpleBeanDecorationFactory;
import org.eclipse.scout.rt.platform.config.AbstractConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.IConfigPropertyWithStatus;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.PlatformDevModeProperty;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.service.IService;

/**
 * @since 15.1
 */
public class PlatformImplementor implements IPlatform {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PlatformImplementor.class);

  private final ReentrantReadWriteLock m_stateLock;
  private volatile State m_state; // may be read at any time by any thread
  private BeanManagerImplementor m_beanContext;

  public PlatformImplementor() {
    m_stateLock = new ReentrantReadWriteLock(true);
    m_state = State.PlatformStopped;
  }

  @Override
  public State getState() {
    return m_state;
  }

  @Override
  public IBeanManager getBeanManager() {
    // use lock to ensure the caller waits until the platform has been started completely
    m_stateLock.readLock().lock();
    try {
      return m_beanContext;
    }
    finally {
      m_stateLock.readLock().unlock();
    }
  }

  @Override
  public synchronized void start() {
    m_stateLock.writeLock().lock();
    try {
      changeState(State.PlatformStopped, true);
      m_beanContext = createBeanManager();
      //now all IPlatformListener are registered and can receive platform events

      changeState(State.BeanManagerPrepared, true);

      //validateBeanManager();
      validateConfiguration();
      initBeanScopeEvaluator();
      initBeanDecorationFactory();

      changeState(State.BeanManagerValid, true);
      startCreateImmediatelyBeans();
    }
    finally {
      m_stateLock.writeLock().unlock();
    }

    // last event is outside lock to allow the listeners to use the bean context and the inventory
    changeState(State.PlatformStarted, true);
  }

  protected void validateConfiguration() {
    if (!ConfigIniUtility.isInitialized()) {
      LOG.warn(": No " + ConfigIniUtility.CONFIG_INI + " found. Running with empty configuration.", new Exception("origin"));
    }

    List<IConfigPropertyWithStatus> configs = BEANS.all(IConfigPropertyWithStatus.class);
    AbstractConfigProperty.checkStatus(configs);
  }

  protected BeanManagerImplementor createBeanManager() {
    BeanManagerImplementor context = new BeanManagerImplementor();
    Set<Class> allBeans = new BeanFilter().collect(ClassInventory.get());
    for (Class<?> bean : allBeans) {
      context.registerClass(bean);
    }
    return context;
  }

  protected void initBeanScopeEvaluator() {
    if (m_beanContext.getScopeEvaluator() != null) {
      return;
    }
    IBean<IBeanScopeEvaluator> bean = m_beanContext.optBean(IBeanScopeEvaluator.class);
    if (bean != null) {
      m_beanContext.setScopeEvaluator(bean.getInstance(IBeanScopeEvaluator.class));
    }
  }

  protected void initBeanDecorationFactory() {
    if (m_beanContext.getBeanDecorationFactory() != null) {
      return;
    }
    IBean<IBeanDecorationFactory> bean = m_beanContext.optBean(IBeanDecorationFactory.class);
    if (bean != null) {
      m_beanContext.setBeanDecorationFactory(bean.getInstance(IBeanDecorationFactory.class));
      return;
    }
    LOG.warn("Using " + SimpleBeanDecorationFactory.class.getName() + ". Please verify that this application really has no client or server side " + IBeanDecorationFactory.class.getSimpleName());
    m_beanContext.setBeanDecorationFactory(new SimpleBeanDecorationFactory());
  }

  protected void validateBeanManager() {
    try {
      //collect all service interfaces
      HashSet<Class> serviceInterfaces = new HashSet<>();
      for (IBean bean : getBeanManager().getRegisteredBeans(IService.class)) {
        for (Class<?> i : BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class)) {
          if (IService.class.isAssignableFrom(i)) {
            serviceInterfaces.add(i);
          }
        }
      }
      for (Class s : serviceInterfaces) {
        if (s.equals(IService.class)) {
          continue;
        }
        try {
          @SuppressWarnings("unchecked")
          List<IBean<Object>> list = getBeanManager().getBeans(s);
          if (list.size() <= 1) {
            continue;
          }
          System.out.println("-------- " + s.getName() + " --------");
          for (IBean<?> bean : list) {
            System.out.println(" @Order(" + BeanHierarchy.orderOf(bean) + ") " + bean.getBeanClazz());
          }
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    catch (Exception e) {
      //nop
    }
  }

  protected void startCreateImmediatelyBeans() {
    ((BeanManagerImplementor) m_beanContext).startCreateImmediatelyBeans();
  }

  @Override
  public synchronized void stop() {
    m_stateLock.writeLock().lock();
    try {
      changeState(State.PlatformStopping, false);
      if (Platform.get() == this) {
        Platform.set(null);
      }
      changeState(State.PlatformStopped, false);
      destroyBeanManager();
    }
    finally {
      m_stateLock.writeLock().unlock();
    }
  }

  protected void destroyBeanManager() {
    m_beanContext = null;
  }

  protected void changeState(State newState, boolean verify) {
    if (verify) {
      verifyStateChange(m_state, newState);
    }
    if (m_state == newState) {
      return;
    }
    m_state = newState;
    fireStateEvent(newState);
  }

  protected void verifyStateChange(State oldState, State newState) {
    if (oldState == newState) {
      return;
    }
    if (oldState == State.PlatformStopped && newState == State.BeanManagerPrepared) {
      return;
    }
    else if (oldState == State.BeanManagerPrepared && newState == State.BeanManagerValid) {
      return;
    }
    else if (oldState == State.BeanManagerValid && newState == State.PlatformStarted) {
      return;
    }

    else if (oldState == State.PlatformStarted && newState == State.PlatformStopping) {
      return;
    }
    else if (oldState == State.PlatformStopping && newState == State.PlatformStopped) {
      return;
    }
    throw new PlatformException("Invalid state change from " + oldState + " to " + newState);
  }

  protected void fireStateEvent(State newState) {
    try {
      PlatformEvent e = new PlatformEvent(this, newState);
      for (IBean<IPlatformListener> bean : m_beanContext.getBeans(IPlatformListener.class)) {
        try {
          IPlatformListener listener = bean.getInstance(IPlatformListener.class);
          listener.stateChanged(e);
        }
        catch (Exception ex) {
          LOG.warn(IPlatformListener.class.getSimpleName() + " " + bean.getBeanClazz(), ex);
        }
      }
    }
    catch (Exception ex) {
      LOG.warn("state " + newState, ex);
    }
  }

  @Override
  public boolean inDevelopmentMode() {
    return CONFIG.getPropertyValue(PlatformDevModeProperty.class); // cannot be null
  }
}
