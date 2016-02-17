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
package org.eclipse.scout.rt.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.clientnotification.IClientNodeId;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.extension.IServerSessionExtension;
import org.eclipse.scout.rt.server.extension.ServerSessionChains.ServerSessionLoadSessionChain;
import org.eclipse.scout.rt.shared.ScoutTexts;
import org.eclipse.scout.rt.shared.extension.AbstractSerializableExtension;
import org.eclipse.scout.rt.shared.extension.IExtensibleObject;
import org.eclipse.scout.rt.shared.extension.IExtension;
import org.eclipse.scout.rt.shared.extension.ObjectExtensions;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.shared.services.common.context.SharedContextChangedNotification;
import org.eclipse.scout.rt.shared.services.common.context.SharedVariableMap;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.session.IGlobalSessionListener;
import org.eclipse.scout.rt.shared.session.ISessionListener;
import org.eclipse.scout.rt.shared.session.SessionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServerSession implements IServerSession, Serializable, IExtensibleObject {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(AbstractServerSession.class);

  private final transient EventListenerList m_eventListeners;

  private String m_id;
  private boolean m_initialized;
  private boolean m_active;
  private final Map<String, Object> m_attributes;
  private transient Object m_attributesLock;
  private final SharedVariableMap m_sharedVariableMap;
  private transient ScoutTexts m_scoutTexts;
  private final ObjectExtensions<AbstractServerSession, IServerSessionExtension<? extends AbstractServerSession>> m_objectExtensions;

  public AbstractServerSession(boolean autoInitConfig) {
    m_eventListeners = new EventListenerList();
    m_attributesLock = new Object();
    m_attributes = new HashMap<>();
    m_sharedVariableMap = new SharedVariableMap();
    m_objectExtensions = new ObjectExtensions<AbstractServerSession, IServerSessionExtension<? extends AbstractServerSession>>(this);
    m_scoutTexts = new ScoutTexts();
    if (autoInitConfig) {
      interceptInitConfig();
    }
  }

  /**
   * This method is used for deserialization.
   */
  private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
    ois.defaultReadObject();
    if (m_scoutTexts == null) {
      m_scoutTexts = new ScoutTexts();
    }

    if (m_attributesLock == null) {
      m_attributesLock = new Object();
    }
  }

  @Override
  public Map<String, Object> getSharedVariableMap() {
    return CollectionUtility.copyMap(m_sharedVariableMap);
  }

  /**
   * do not use this internal method directly
   */
  protected <T> T getSharedContextVariable(String name, Class<T> type) {
    Object o = m_sharedVariableMap.get(name);
    return TypeCastUtility.castValue(o, type);
  }

  /**
   * do not use this internal method directly
   */
  protected <T> void setSharedContextVariable(String name, Class<T> type, T value) {
    T typedValue = TypeCastUtility.castValue(value, type);
    m_sharedVariableMap.put(name, typedValue);
  }

  private void assignUserId() {
    String userId = BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    setUserIdInternal(userId);
  }

  /**
   * The session is running in its event loop
   */
  @Override
  public boolean isActive() {
    return m_active;
  }

  @Override
  public final String getUserId() {
    return getSharedContextVariable("userId", String.class);
  }

  private void setUserIdInternal(String newValue) {
    setSharedContextVariable("userId", String.class, newValue);
  }

  /**
   * <p>
   * Returns the {@link ScoutTexts} instance assigned to the type (class) of the current ServerSession.
   * </p>
   * <p>
   * Override this method to set the application specific texts implementation
   * </p>
   */
  @Override
  public ScoutTexts getTexts() {
    return m_scoutTexts;
  }

  @Override
  public Object getData(String key) {
    synchronized (m_attributesLock) {
      return m_attributes.get(key);
    }
  }

  @Override
  public void setData(String key, Object value) {
    synchronized (m_attributesLock) {
      m_attributes.put(key, value);
    }
  }

  protected final void interceptInitConfig() {
    m_objectExtensions.initConfig(createLocalExtension(), new Runnable() {
      @Override
      public void run() {
        initConfig();
      }
    });
  }

  protected void initConfig() {
    m_sharedVariableMap.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        if (IClientNodeId.CURRENT.get() != null) {
          String sessionId = getId();
          if (sessionId != null) {
            SharedContextChangedNotification notification = new SharedContextChangedNotification(new SharedVariableMap(m_sharedVariableMap));
            BEANS.get(ClientNotificationRegistry.class).putTransactionalForSession(sessionId, notification);
          }
          else {
            LOG.warn("No sessionId set");
          }
        }
      }
    });
    if (m_initialized) {
      return;
    }
    m_initialized = true;
  }

  @Override
  public final List<? extends IServerSessionExtension<? extends AbstractServerSession>> getAllExtensions() {
    return m_objectExtensions.getAllExtensions();
  }

  protected IServerSessionExtension<? extends AbstractServerSession> createLocalExtension() {
    return new LocalServerSessionExtension<AbstractServerSession>(this);
  }

  @Override
  public <T extends IExtension<?>> T getExtension(Class<T> c) {
    return m_objectExtensions.getExtension(c);
  }

  @Override
  public final void start(String sessionId) {
    Assertions.assertNotNull(sessionId, "Session id must not be null");
    m_id = sessionId;
    Assertions.assertFalse(isActive(), "Session already started");
    assignUserId();
    interceptLoadSession();

    m_active = true;

    fireSessionChangedEvent(new SessionEvent(this, SessionEvent.TYPE_STARTED));
    LOG.info("Server session started [session={}, user={}]", this, getUserId());
  }

  /**
   * 1. Identify (authenticate) user by its credentials
   * <ul>
   * <li>success: nop
   * <li>failure: throws wrapped ProcessingException(SecurityException)
   * </ul>
   * 2. Load session data such as properties, permissions
   */
  @ConfigOperation
  @Order(10)
  protected void execLoadSession() {
  }

  @Override
  public String getId() {
    return m_id;
  }

  /**
   * The extension delegating to the local methods. This Extension is always at the end of the chain and will not call
   * any further chain elements.
   */
  protected static class LocalServerSessionExtension<OWNER extends AbstractServerSession> extends AbstractSerializableExtension<OWNER> implements IServerSessionExtension<OWNER> {
    private static final long serialVersionUID = 1L;

    public LocalServerSessionExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execLoadSession(ServerSessionLoadSessionChain chain) {
      getOwner().execLoadSession();
    }

  }

  protected final void interceptLoadSession() {
    List<? extends IServerSessionExtension<? extends AbstractServerSession>> extensions = getAllExtensions();
    ServerSessionLoadSessionChain chain = new ServerSessionLoadSessionChain(extensions);
    chain.execLoadSession();
  }

  @Override
  public void stop() {
    m_active = false;

    fireSessionChangedEvent(new SessionEvent(this, SessionEvent.TYPE_STOPPED));

    // Cancel globally registered RunMonitors of this session.
    BEANS.get(RunMonitorCancelRegistry.class).cancelAllBySessionId(getId());

    // Cancel running jobs of this session.
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(this))
        .andMatchNotFuture(IFuture.CURRENT.get())
        .toFilter(), true);

    LOG.info("Server session stopped [session={}, user={}]", this, getUserId());
  }

  @Override
  public void addListener(ISessionListener sessionListener) {
    m_eventListeners.add(ISessionListener.class, sessionListener);
  }

  @Override
  public void removeListener(ISessionListener sessionListener) {
    m_eventListeners.remove(ISessionListener.class, sessionListener);
  }

  @Internal
  protected void fireSessionChangedEvent(final SessionEvent event) {
    List<ISessionListener> listeners = new ArrayList<>();
    listeners.addAll(Arrays.asList(m_eventListeners.getListeners(ISessionListener.class))); // session specific listeners
    listeners.addAll(BEANS.all(IGlobalSessionListener.class)); // global listeners
    for (final ISessionListener listener : listeners) {
      listener.sessionChanged(event);
    }
  }

  @Override
  public String toString() {
    return super.toString() + "[id = " + getId() + "]";
  }
}
