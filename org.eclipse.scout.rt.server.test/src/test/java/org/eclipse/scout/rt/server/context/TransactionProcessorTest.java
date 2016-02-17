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
package org.eclipse.scout.rt.server.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanInstanceProducer;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.holders.Holder;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.server.transaction.TransactionRequiredException;
import org.eclipse.scout.rt.server.transaction.TransactionScope;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(PlatformTestRunner.class)
public class TransactionProcessorTest {

  private List<IBean<?>> m_beans;

  @Mock
  private ITransaction m_transaction;

  private List<Throwable> m_txErrors;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    m_txErrors = new ArrayList<>();
    m_beans = TestingUtility.registerBeans(new BeanMetaData(ITransaction.class).withOrder(-1000).withProducer(new IBeanInstanceProducer<ITransaction>() {
      @Override
      public ITransaction produce(IBean<ITransaction> bean) {
        return m_transaction;
      }
    }), new BeanMetaData(ITransactionCommitProtocol.class).withOrder(Long.MAX_VALUE));

    // mock the transaction
    // ITransaction.commitPhase1
    when(m_transaction.commitPhase1()).thenReturn(true);

    // ITransaction.addFailure
    doAnswer(new Answer<Void>() {

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        m_txErrors.add((Throwable) invocation.getArguments()[0]);
        return null;
      }
    }).when(m_transaction).addFailure(any(Throwable.class));

    // ITransaction.hasFailures
    doAnswer(new Answer<Boolean>() {

      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        return !m_txErrors.isEmpty();
      }
    }).when(m_transaction).hasFailures();

    RunMonitor.CURRENT.set(new RunMonitor());
  }

  @After
  public void after() {
    RunMonitor.CURRENT.remove();
    TestingUtility.unregisterBeans(m_beans);
    m_beans.clear();
    m_txErrors.clear();
  }

  @Test
  public void testMandatoryWithoutExistingTransaction() throws Exception {
    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(null, TransactionScope.MANDATORY));

    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          return "result";
        }
      });
      fail();
    }
    catch (TransactionRequiredException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testMandatoryWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.MANDATORY));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    assertEquals("result", result);
    assertSame(callingTransaction, actualTransaction.getValue());
    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testMandatoryWithExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException();
    ITransaction callingTransaction = mock(ITransaction.class);

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.MANDATORY));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (Exception e) {
      assertSame(exception, e);

      assertSame(callingTransaction, actualTransaction.getValue());
      verifyZeroInteractions(m_transaction);
      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, never()).release();
      verify(callingTransaction, times(1)).addFailure(any(Exception.class));
    }
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndSuccess() throws Exception {
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(null, TransactionScope.REQUIRES_NEW));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertSame(m_transaction, actualTransaction.getValue());
    assertEquals("result", result);

    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithoutExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException();

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(null, TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);
      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.REQUIRES_NEW));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertSame(m_transaction, actualTransaction.getValue());
    assertEquals("result", result);

    verifyZeroInteractions(callingTransaction);
    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiresNewWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final RuntimeException exception = new RuntimeException();

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verifyZeroInteractions(callingTransaction);
      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndSuccess() throws Exception {
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(null, TransactionScope.REQUIRED));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertEquals("result", result);
    assertSame(m_transaction, actualTransaction.getValue());

    verify(m_transaction, times(1)).release();

    InOrder inOrder = Mockito.inOrder(m_transaction);
    inOrder.verify(m_transaction, times(1)).commitPhase1();
    inOrder.verify(m_transaction, times(1)).commitPhase2();
    inOrder.verify(m_transaction, never()).rollback();
    inOrder.verify(m_transaction, times(1)).release();
  }

  @Test
  public void testRequiredWithoutExistingTransactionAndError() throws Exception {
    final RuntimeException exception = new RuntimeException();

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(null, TransactionScope.REQUIRES_NEW));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(m_transaction, actualTransaction.getValue());

      verify(m_transaction, times(1)).release();

      InOrder inOrder = Mockito.inOrder(m_transaction);

      inOrder.verify(m_transaction, never()).commitPhase1();
      inOrder.verify(m_transaction, never()).commitPhase2();
      inOrder.verify(m_transaction, times(1)).rollback();
      inOrder.verify(m_transaction, times(1)).release();
    }
  }

  @Test
  public void testRequiredWithExistingTransactionAndSuccess() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);
    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.REQUIRED));
    Object result = chain.call(new Callable<Object>() {

      @Override
      public Object call() throws Exception {
        actualTransaction.setValue(ITransaction.CURRENT.get());
        return "result";
      }
    });

    // verify
    assertEquals("result", result);
    assertSame(callingTransaction, actualTransaction.getValue());

    verifyZeroInteractions(m_transaction);
    verifyZeroInteractions(callingTransaction);
  }

  @Test
  public void testRequiredWithExistingTransactionAndError() throws Exception {
    ITransaction callingTransaction = mock(ITransaction.class);

    final RuntimeException exception = new RuntimeException();

    final Holder<ITransaction> actualTransaction = new Holder<>();

    CallableChain<Object> chain = new CallableChain<>();
    chain.add(new TransactionProcessor(callingTransaction, TransactionScope.REQUIRED));
    try {
      chain.call(new Callable<Object>() {

        @Override
        public Object call() throws Exception {
          actualTransaction.setValue(ITransaction.CURRENT.get());
          throw exception;
        }
      });
      fail();
    }
    catch (RuntimeException e) {
      assertSame(exception, e);
      assertSame(callingTransaction, actualTransaction.getValue());

      verifyZeroInteractions(m_transaction);

      verify(callingTransaction, never()).commitPhase1();
      verify(callingTransaction, never()).commitPhase2();
      verify(callingTransaction, never()).rollback();
      verify(callingTransaction, times(1)).addFailure(any(Throwable.class));
    }
  }
}
