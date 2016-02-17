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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.util.BlockingCountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class WhenDoneTest {

  @Test
  public void testSuccess() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getException());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  @Test
  public void testSuccessWithJobAlreadyCompleted() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));
    future.awaitDoneAndGet();

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getException());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  @Test
  public void testError() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();
    final ProcessingException pe = new ProcessingException();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        throw pe;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertSame(pe, eventHolder.getValue().getException());
    assertNull(eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertTrue(eventHolder.getValue().isFailed());
  }

  @Test
  public void testErrorWithJobAlreadyCompleted() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();
    final Exception error = new ProcessingException();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        throw error;
      }
    }, Jobs.newInput()
        .withExceptionHandling(null, false));
    try {
      future.awaitDoneAndGet();
      fail("exception expected");
    }
    catch (ProcessingException e) {
      final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
      future.whenDone(new IDoneHandler<String>() {

        @Override
        public void onDone(DoneEvent<String> event) {
          protocol.add("2");
          if (future.isDone()) {
            protocol.add("done");
          }
          if (future.isCancelled()) {
            protocol.add("cancelled");
          }
          eventHolder.setValue(event);
          verifyLatch.countDown();
        }
      }, RunContexts.copyCurrent());

      assertTrue(verifyLatch.await());
      assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
      assertSame(error, eventHolder.getValue().getException());
      assertNull(eventHolder.getValue().getResult());
      assertFalse(eventHolder.getValue().isCancelled());
      assertTrue(eventHolder.getValue().isFailed());
    }
  }

  @Test
  public void testCancel() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent())
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)));

    future.cancel(true);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("2", "done", "cancelled"), protocol);
    assertNull(eventHolder.getValue().getException());
    assertNull(eventHolder.getValue().getResult());
    assertTrue(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }

  /**
   * Tests that 'Future.whenDone' returns once the Future is cancelled, even if that job is still runnning.
   */
  @Test
  public void testCancelButStillRunning() throws InterruptedException {
    final BlockingCountDownLatch setupLatch = new BlockingCountDownLatch(1);
    final BlockingCountDownLatch continueRunningLatch = new BlockingCountDownLatch(1);

    final IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        try {
          setupLatch.countDownAndBlock();
        }
        catch (InterruptedException e) {
          Thread.interrupted(); // ensure the thread's interrupted status to be cleared in order to continue the test.
          continueRunningLatch.countDownAndBlock(); // continue running
        }
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    assertTrue(setupLatch.await());

    // run the test.
    future.cancel(true);

    // verify that whenDone immediately returns even if not completed yet.
    final AtomicBoolean onDone = new AtomicBoolean(false);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<Void>() {

      @Override
      public void onDone(DoneEvent<Void> event) {
        onDone.set(true);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertTrue(onDone.get());
    continueRunningLatch.release();
  }

  @Test
  public void testCancelWithJobAlreadyCompleted() throws InterruptedException {
    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final Holder<DoneEvent<String>> eventHolder = new Holder<>();

    final IFuture<String> future = Jobs.schedule(new Callable<String>() {

      @Override
      public String call() throws Exception {
        protocol.add("1");
        return "result";
      }
    }, Jobs.newInput()
        .withRunContext(RunContexts.copyCurrent()));

    future.awaitDoneAndGet();
    future.cancel(true);

    final BlockingCountDownLatch verifyLatch = new BlockingCountDownLatch(1);
    future.whenDone(new IDoneHandler<String>() {

      @Override
      public void onDone(DoneEvent<String> event) {
        protocol.add("2");
        if (future.isDone()) {
          protocol.add("done");
        }
        if (future.isCancelled()) {
          protocol.add("cancelled");
        }
        eventHolder.setValue(event);
        verifyLatch.countDown();
      }
    }, RunContexts.copyCurrent());
    assertTrue(verifyLatch.await());

    assertEquals(CollectionUtility.arrayList("1", "2", "done"), protocol);
    assertNull(eventHolder.getValue().getException());
    assertEquals("result", eventHolder.getValue().getResult());
    assertFalse(eventHolder.getValue().isCancelled());
    assertFalse(eventHolder.getValue().isFailed());
  }
}
