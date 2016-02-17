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
package org.eclipse.scout.rt.platform.job.filter.future;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class ExecutionHintFutureFilterTest {

  @Test
  public void test() {
    // job1
    IFuture<Void> future1 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput()
        .withExecutionHint("ui-interaction-required"));

    // job2
    IFuture<Void> future2 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IFuture.CURRENT.get().removeExecutionHint("ui-interaction-required");
      }
    }, Jobs.newInput().withExecutionHint("ui-interaction-required"));

    // job3
    IFuture<Void> future3 = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        IFuture.CURRENT.get().addExecutionHint("ui-interaction-required");
      }
    }, Jobs.newInput());

    // job4
    IFuture<Void> future4 = Jobs.schedule(mock(IRunnable.class), Jobs.newInput());
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchFuture(
            future1,
            future2,
            future3,
            future4)
        .toFilter(), 10, TimeUnit.SECONDS);

    IFilter<IFuture<?>> filter = new ExecutionHintFutureFilter("ui-interaction-required");
    assertTrue(filter.accept(future1)); // hint added by job input
    assertFalse(filter.accept(future2)); // hint is removed while running
    assertTrue(filter.accept(future3)); // hint added while running
    assertFalse(filter.accept(future4));
  }
}
