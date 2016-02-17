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
package org.eclipse.scout.rt.testing.server.runner;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.reflect.ReflectionUtility;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.statement.ClearServerRunContextStatement;
import org.eclipse.scout.rt.testing.server.runner.statement.ClientNotificationsStatement;
import org.eclipse.scout.rt.testing.server.runner.statement.ServerRunContextStatement;
import org.eclipse.scout.rt.testing.server.runner.statement.TimeoutServerRunContextStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Test.None;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Use this Runner to run tests which require a session and transaction context.
 * <p/>
 * Use the following mandatory annotations to configure the Runner:
 * <ul>
 * <li><strong>RunWithServerSession</strong>:<br/>
 * to specify the server-session to be used; can be defined on class or method-level;</li>
 * <li><strong>RunWithSubject</strong>:<br/>
 * to specify the user on behalf of which to run the test; can be defined on class or method-level;</li>
 * </ul>
 * Example:
 *
 * <pre>
 * &#064;RunWith(ServerTestRunner.class)
 * &#064;RunWithServerSession()
 * &#064;RunWithSubject(&quot;anna&quot;)
 * public class YourTest {
 *    ...
 * }
 * </pre>
 *
 * Execution:
 * <ul>
 * <li>Each test-method is executed in a separate transaction - meaning that the transaction boundary starts before
 * executing the first 'before-method', and ends after executing the last 'after-method'.</li>
 * <li>By default, server sessions are shared among same users. This can be changed by setting the
 * {@link ServerSessionProvider} or a custom provider to {@link RunWithServerSession#provider()}.</li>
 * <li>'beforeClass' and 'afterClass' are executed in the same transaction.</li>
 * </ul>
 * <b>Note</b>: Usually, all {@link Before}, the {@link Test}-annotated method and all {@link After} methods are invoked
 * in a single transaction. But if the {@link Test}-annotated method uses the timeout feature (i.e.
 * {@link Test#timeout()}), the three parts are executed in different transactions.
 *
 * @see RunWithServerSession
 * @see RunWithSubject
 * @since 5.1
 */
public class ServerTestRunner extends PlatformTestRunner {

  public ServerTestRunner(final Class<?> clazz) throws InitializationError {
    super(clazz);
  }

  @Override
  protected Statement interceptClassLevelStatement(final Statement next, final Class<?> testClass) {
    final Statement s4 = new ServerRunContextStatement(next, ReflectionUtility.getAnnotation(RunWithServerSession.class, testClass));
    final Statement s3 = new ClientNotificationsStatement(s4, ReflectionUtility.getAnnotation(RunWithClientNotifications.class, testClass));
    final Statement s2 = super.interceptClassLevelStatement(s3, testClass);
    final Statement s1 = new ClearServerRunContextStatement(s2);

    return s1;
  }

  protected boolean expectsException(final Test annotation) {
    return annotation != null && annotation.expected() != None.class;
  }

  @Override
  protected Statement interceptMethodLevelStatement(final Statement next, final Class<?> testClass, final Method testMethod) {
    final Statement s4 = new ServerRunContextStatement(next, ReflectionUtility.getAnnotation(RunWithServerSession.class, testMethod, testClass));
    final Statement s3 = new ClientNotificationsStatement(s4, ReflectionUtility.getAnnotation(RunWithClientNotifications.class, testClass));
    final Statement s2 = super.interceptMethodLevelStatement(s3, testClass, testMethod);
    final Statement s1 = new ClearServerRunContextStatement(s2);
    return s1;
  }

  @Override
  @SuppressWarnings("deprecation")
  protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
    long timeoutMillis = getTimeoutMillis(method.getMethod());
    if (timeoutMillis <= 0) {
      return next;
    }
    return new TimeoutServerRunContextStatement(next, timeoutMillis);
  }

  @Override
  protected Statement interceptBeforeStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedBeforeStatement = super.interceptBeforeStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      return interceptedBeforeStatement;
    }
    return new TimeoutServerRunContextStatement(interceptedBeforeStatement, 0);
  }

  @Override
  protected Statement interceptAfterStatement(Statement next, Class<?> testClass, Method testMethod) {
    Statement interceptedAfterStatement = super.interceptAfterStatement(next, testClass, testMethod);
    if (hasNoTimeout(testMethod)) {
      return interceptedAfterStatement;
    }
    return new TimeoutServerRunContextStatement(interceptedAfterStatement, 0);
  }
}
