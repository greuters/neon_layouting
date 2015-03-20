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
package org.eclipse.scout.rt.platform.job.internal.callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.holders.StringHolder;
import org.eclipse.scout.rt.platform.AnnotationFactory;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.platform.internal.BeanImplementor;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.JobManager;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ThreadNameDecoratorTest {

  private JobManager m_jobManager;
  private BeanImplementor<JobManager> m_bean;

  @Before
  public void before() {
    m_jobManager = new JobManager();

    m_bean = new BeanImplementor<>(JobManager.class);
    m_bean.addAnnotation(AnnotationFactory.createApplicationScoped());
    m_bean.addAnnotation(AnnotationFactory.createPriority(10000));
    Platform.get().getBeanContext().registerBean(m_bean, m_jobManager);
  }

  @After
  public void after() {
    m_jobManager.shutdown();
    Platform.get().getBeanContext().unregisterBean(m_bean);
  }

  @Test
  public void testThreadName() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    JobInput input = JobInput.empty().id("123").name("job1");

    NamedThreadFactory.CURRENT_THREAD_INFO.set(new ThreadInfo("scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.getIdentifier()).call();
    assertEquals("scout-client-thread-5 [Running] 123:job1", threadName.getValue());
    assertEquals("scout-thread-5 [Idle]", Thread.currentThread().getName());
    NamedThreadFactory.CURRENT_THREAD_INFO.remove();
  }

  @Test
  public void testThreadNameWithEmptyJobIdentifier() throws Exception {
    final StringHolder threadName = new StringHolder();

    Callable<Void> next = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        threadName.setValue(Thread.currentThread().getName());
        return null;
      }
    };

    JobInput input = JobInput.empty();

    NamedThreadFactory.CURRENT_THREAD_INFO.set(new ThreadInfo("scout-thread", 5));
    new ThreadNameDecorator<Void>(next, "scout-client-thread", input.getIdentifier()).call();
    assertEquals("scout-client-thread-5 [Running]", threadName.getValue());
    assertEquals("scout-thread-5 [Idle]", Thread.currentThread().getName());
    NamedThreadFactory.CURRENT_THREAD_INFO.remove();
  }

  @Test
  public void testThreadNameBlocking() throws Exception {
    final Object mutexObject = new Object();
    final IBlockingCondition BC = m_jobManager.createBlockingCondition("blocking-condition", true);
    final Holder<Thread> threadJob1 = new Holder<>();

    // Job-1 (same mutex as job-2)
    IFuture<Boolean> future1 = m_jobManager.schedule(new ICallable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        threadJob1.setValue(Thread.currentThread());

        String currentThreadName = Thread.currentThread().getName();
        assertTrue(currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\[Running\\] job-1"));

        // Start blocking
        BC.waitFor();

        currentThreadName = Thread.currentThread().getName();
        assertTrue(currentThreadName, currentThreadName.matches("scout-thread-\\d+ \\[Running\\] job-1"));
        return true;
      }
    }, JobInput.defaults().name("job-1").mutex(mutexObject));

    // Job-2 (same mutex as job-1)
    IFuture<Boolean> future2 = m_jobManager.schedule(new ICallable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        String threadNameJob1 = threadJob1.getValue().getName();

        assertTrue(threadNameJob1, threadJob1.getValue().getName().matches("scout-thread-\\d+ \\[Blocked 'blocking-condition'\\] job-1"));

        // Release job-1
        BC.setBlocking(false);

        return true;
      }
    }, JobInput.defaults().name("job-1").mutex(mutexObject));

    assertTrue(future2.awaitDoneAndGet());
    assertTrue(future1.awaitDoneAndGet());
  }
}
