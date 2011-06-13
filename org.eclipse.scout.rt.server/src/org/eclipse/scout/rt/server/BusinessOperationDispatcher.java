/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Locale;

import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.server.admin.inspector.CallInspector;
import org.eclipse.scout.rt.server.admin.inspector.ProcessInspector;
import org.eclipse.scout.rt.server.admin.inspector.SessionInspector;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.rt.shared.services.common.exceptionhandler.IExceptionHandlerService;
import org.eclipse.scout.rt.shared.services.common.security.ACCESS;
import org.eclipse.scout.rt.shared.servicetunnel.RemoteServiceAccessDenied;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelAccessDenied;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.VersionMismatchException;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.IService2;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * delegate for scout dynamic business op invocation
 */
@SuppressWarnings("deprecation")
public class BusinessOperationDispatcher {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(BusinessOperationDispatcher.class);

  private final Version m_requestMinVersion;
  private final boolean m_debug;
  private final Bundle[] m_loaderBundles;
  private long m_requestStart;
  private long m_requestEnd;

  public BusinessOperationDispatcher(Bundle[] loaderBundles, Version requestMinVersion, boolean debug) {
    m_loaderBundles = loaderBundles;
    m_requestMinVersion = requestMinVersion;
    m_debug = debug;
  }

  public ServiceTunnelResponse invoke(ServiceTunnelRequest serviceReq) throws Exception {
    ServiceTunnelResponse response;
    m_requestStart = System.nanoTime();
    try {
      response = invokeImpl(serviceReq);
    }
    catch (Throwable t) {
      try {
        // cancel tx
        ITransaction transaction = ThreadContext.get(ITransaction.class);
        if (transaction != null) {
          transaction.addFailure(t);
        }
      }
      catch (Throwable ignore) {
        // nop
      }
      //log it
      if (t instanceof ProcessingException) {
        SERVICES.getService(IExceptionHandlerService.class).handleException((ProcessingException) t);
      }
      else {
        LOG.error("invoking " + serviceReq.getServiceInterfaceClassName() + ":" + serviceReq.getOperation(), t);
      }
      response = new ServiceTunnelResponse(null, null, t);
    }
    finally {
      if (m_debug) {
        LOG.debug("TIME " + serviceReq.getServiceInterfaceClassName() + "." + serviceReq.getOperation() + " " + (m_requestEnd - m_requestStart) / 1000000L + "ms");
      }
    }
    m_requestEnd = System.nanoTime();
    response.setProcessingDuration((m_requestEnd - m_requestStart) / 1000000L);
    return response;
  }

  /**
   * This method is executed within a {@link IServerSession} context using a {@link ServerJob}
   */
  protected ServiceTunnelResponse invokeImpl(ServiceTunnelRequest serviceReq) throws Throwable {
    String soapOperation = ServiceTunnelRequest.toSoapOperation(serviceReq.getServiceInterfaceClassName(), serviceReq.getOperation());
    IServerSession serverSession = ThreadContext.get(IServerSession.class);
    String authenticatedUser = serverSession.getUserId();
    if (LOG.isDebugEnabled()) {
      LOG.debug("started " + serviceReq.getServiceInterfaceClassName() + "." + serviceReq.getOperation() + " by " + authenticatedUser + " at " + new Date());
    }
    // version check of request
    if (m_requestMinVersion != null) {
      String v = serviceReq.getVersion();
      if (v == null) {
        v = "0.0.0";
      }
      Version requestVersion = Version.parseVersion(v);
      if (requestVersion.compareTo(m_requestMinVersion) < 0) {
        ServiceTunnelResponse serviceRes = new ServiceTunnelResponse(null, null, new VersionMismatchException(requestVersion.toString(), m_requestMinVersion.toString()));
        return serviceRes;
      }
    }
    CallInspector callInspector = null;
    SessionInspector sessionInspector = ProcessInspector.getDefault().getSessionInspector(serverSession, true);
    if (sessionInspector != null) {
      callInspector = sessionInspector.requestCallInspector(serviceReq);
    }
    ServiceTunnelResponse serviceRes = null;
    try {
      //do checks
      Class<?> serviceInterfaceClass = null;
      for (Bundle b : m_loaderBundles) {
        try {
          serviceInterfaceClass = b.loadClass(serviceReq.getServiceInterfaceClassName());
          break;
        }
        catch (ClassNotFoundException e) {
          // nop
        }
      }
      //check access: existence
      if (serviceInterfaceClass == null) {
        throw new ClassNotFoundException(serviceReq.getServiceInterfaceClassName());
      }
      //check access: service proxy allowed
      Method serviceOp = ServiceUtility.getServiceOperation(serviceInterfaceClass, serviceReq.getOperation(), serviceReq.getParameterTypes());
      checkRemoteServiceAccessByInterface(serviceInterfaceClass, serviceOp, serviceReq.getArgs());
      //check access: service impl exists
      Object service = SERVICES.getService(serviceInterfaceClass);
      if (service == null) {
        throw new SecurityException("service registry does not contain a service of type " + serviceReq.getServiceInterfaceClassName());
      }
      checkRemoteServiceAccessByAnnotations(serviceInterfaceClass, service.getClass(), serviceOp, serviceReq.getArgs());
      checkRemoteServiceAccessByPermission(serviceInterfaceClass, service.getClass(), serviceOp, serviceReq.getArgs());
      //all checks done
      //
      // check if locales changed
      Locale userLocale = LocaleThreadLocal.get();
      NlsLocale userNlsLocale = NlsLocale.getDefault();
      if (CompareUtility.equals(userLocale, serverSession.getLocale()) && CompareUtility.equals(userNlsLocale, serverSession.getNlsLocale())) {
        // ok
      }
      else {
        serverSession.setLocale(userLocale);
        serverSession.setNlsLocale(userNlsLocale);
        if (serverSession instanceof AbstractServerSession) {
          ((AbstractServerSession) serverSession).execLocaleChanged();
        }
      }
      //
      Object data = ServiceUtility.invoke(serviceOp, service, serviceReq.getArgs());
      Object[] outParameters = ServiceUtility.extractHolderArguments(serviceReq.getArgs());
      serviceRes = new ServiceTunnelResponse(data, outParameters, null);
      serviceRes.setSoapOperation(soapOperation);
      // add accumulated client notifications as side-payload
      IClientNotification[] na = SERVICES.getService(IClientNotificationService.class).getNextNotifications(0);
      serviceRes.setClientNotifications(na);
      return serviceRes;
    }
    finally {
      if (callInspector != null) {
        try {
          callInspector.update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.close(serviceRes);
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
        try {
          callInspector.getSessionInspector().update();
        }
        catch (Throwable t) {
          LOG.warn(null, t);
        }
      }
    }
  }

  /**
   * Check pass 1 on type
   */
  protected void checkRemoteServiceAccessByInterface(Class<?> interfaceClass, Method interfaceMethod, Object[] args) {
    //check: must be an interface
    if (!interfaceClass.isInterface()) {
      throw new SecurityException("access denied (code 1a).");
    }
    //check: must be a subclass of IService
    if (!IService.class.isAssignableFrom(interfaceClass)) {
      throw new SecurityException("access denied (code 1b).");
    }
    //check: method is defined on service interface itself
    Method verifyMethod;
    try {
      verifyMethod = interfaceClass.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
    }
    catch (Throwable t) {
      throw new SecurityException("access denied (code 1c).");
    }
    //exists
    if (verifyMethod.getDeclaringClass() == IService.class || verifyMethod.getDeclaringClass() == IService2.class) {
      throw new SecurityException("access denied (code 1d).");
    }
  }

  /**
   * Check pass 2 on instance
   */
  protected void checkRemoteServiceAccessByAnnotations(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    //check: grant/deny annotation (type level is base, method level is finegrained)
    Class<?> c = implClass;
    while (c != null) {
      //method level
      Method m = null;
      try {
        m = c.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
      }
      catch (Throwable t) {
        //nop
      }
      if (m != null) {
        for (Annotation ann : m.getAnnotations()) {
          //legacy
          if (ann.annotationType() == ServiceTunnelAccessDenied.class) {
            throw new SecurityException("access denied (code 2a).");
          }
          if (ann.annotationType() == RemoteServiceAccessDenied.class) {
            throw new SecurityException("access denied (code 2b).");
          }
        }
      }
      //type level
      for (Annotation ann : c.getAnnotations()) {
        if (ann.annotationType() == RemoteServiceAccessDenied.class) {
          throw new SecurityException("access denied (code 2c).");
        }
      }
      //next
      if (c == interfaceClass) {
        break;
      }
      c = c.getSuperclass();
      if (c == Object.class) {
        //use interface at last
        c = interfaceClass;
      }
    }
    //implicit ok
  }

  /**
   * Check {@link RemoteServiceAccessPermission} if a client (gui) is allowed to call this service from remote using a
   * remote service proxy.
   * <p>
   * Deny access by default.
   * <p>
   * Accepts when a {@link RemoteServiceAccessPermission} was implied.
   */
  protected void checkRemoteServiceAccessByPermission(Class<?> interfaceClass, Class<?> implClass, Method interfaceMethod, Object[] args) {
    if (ACCESS.check(new RemoteServiceAccessPermission(interfaceClass.getName(), interfaceMethod.getName()))) {
      return;
    }
    throw new SecurityException("access denied (code 3a).");
  }

}
