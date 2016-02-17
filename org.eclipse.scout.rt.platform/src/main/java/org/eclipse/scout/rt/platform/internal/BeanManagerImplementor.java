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
package org.eclipse.scout.rt.platform.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IBeanDecorationFactory;
import org.eclipse.scout.rt.platform.IBeanManager;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.exception.InitializationException;
import org.eclipse.scout.rt.platform.interceptor.IBeanDecorator;
import org.eclipse.scout.rt.platform.interceptor.internal.BeanProxyImplementor;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.BeanUtility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;

public class BeanManagerImplementor implements IBeanManager {
  private final ReentrantReadWriteLock m_lock;
  private final Map<Class<?>, BeanHierarchy> m_beanHierarchies;
  private IBeanDecorationFactory m_beanDecorationFactory;

  public BeanManagerImplementor() {
    this(null);
  }

  public BeanManagerImplementor(IBeanDecorationFactory f) {
    m_lock = new ReentrantReadWriteLock(true);
    m_beanHierarchies = new HashMap<>();
    m_beanDecorationFactory = f;
  }

  public ReentrantReadWriteLock getReadWriteLock() {
    return m_lock;
  }

  @Internal
  protected <T> List<IBean<T>> querySingle(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      @SuppressWarnings("unchecked")
      BeanHierarchy<T> h = m_beanHierarchies.get(beanClazz);
      if (h == null) {
        return Collections.<IBean<T>> emptyList();
      }
      else {
        List<IBean<T>> singleBean = h.querySingle();
        return getDecoratedBeans(singleBean, beanClazz);
      }
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Internal
  protected <T> List<IBean<T>> queryAll(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      @SuppressWarnings("unchecked")
      BeanHierarchy<T> h = m_beanHierarchies.get(beanClazz);
      if (h == null) {
        return Collections.emptyList();
      }
      List<IBean<T>> allBeans = h.queryAll();
      return getDecoratedBeans(allBeans, beanClazz);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  protected Collection<Class<?>> listImplementedTypes(IBean<?> bean) {
    //interfaces
    Set<Class<?>> set = new LinkedHashSet<>();
    set.addAll(BeanUtility.getInterfacesHierarchy(bean.getBeanClazz(), Object.class));
    //super types
    Class c = bean.getBeanClazz();
    while (c != null && c != Object.class) {
      set.add(c);
      c = c.getSuperclass();
    }
    set.add(Object.class);
    return set;
  }

  protected <T> List<IBean<T>> getDecoratedBeans(List<IBean<T>> beans, Class<T> beanClazz) {
    IBeanDecorationFactory beanDecorationFactory = getBeanDecorationFactory();
    if (beanDecorationFactory == null || !beanClazz.isInterface()) {
      return beans;
    }

    // apply decorations
    List<IBean<T>> result = new ArrayList<IBean<T>>(beans.size());
    for (IBean<T> bean : beans) {
      result.add(getDecoratedBean(bean, beanClazz, beanDecorationFactory));
    }
    return result;
  }

  protected <T> IBean<T> getDecoratedBean(IBean<T> bean, Class<T> beanClazz, IBeanDecorationFactory beanDecorationFactory) {
    IBeanDecorator<T> decorator = beanDecorationFactory.decorate(bean, beanClazz);
    if (decorator == null) {
      return bean;
    }

    T proxy = new BeanProxyImplementor<T>(bean, decorator, beanClazz).getProxy();
    return new BeanImplementor<T>(new BeanMetaData(beanClazz).withInitialInstance(proxy).withAnnotations(bean.getBeanAnnotations().values()));
  }

  @Override
  public <T> IBean<T> registerClass(Class<T> beanClazz) {
    return registerBean(new BeanMetaData(beanClazz));
  }

  @Override
  public <T> void unregisterClass(Class<T> beanClazz) {
    for (IBean<T> bean : getRegisteredBeans(beanClazz)) {
      if (bean.getBeanClazz() == beanClazz) {
        unregisterBean(bean);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> IBean<T> registerBean(BeanMetaData beanData) {
    m_lock.writeLock().lock();
    try {
      IBean<T> bean = new BeanImplementor<T>(beanData);
      for (Class<?> type : listImplementedTypes(bean)) {
        BeanHierarchy h = m_beanHierarchies.get(type);
        if (h == null) {
          h = new BeanHierarchy(type);
          m_beanHierarchies.put(type, h);
        }
        h.addBean(bean);
      }
      return bean;
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public synchronized void unregisterBean(IBean bean) {
    m_lock.writeLock().lock();
    try {
      Assertions.assertNotNull(bean);
      for (Class<?> type : listImplementedTypes(bean)) {
        BeanHierarchy h = m_beanHierarchies.get(type);
        if (h != null) {
          h.removeBean(bean);
        }
      }
      if (bean instanceof BeanImplementor) {
        ((BeanImplementor) bean).dispose();
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> List<IBean<T>> getRegisteredBeans(Class<T> beanClazz) {
    m_lock.readLock().lock();
    try {
      BeanHierarchy<T> h = m_beanHierarchies.get(beanClazz);
      if (h == null) {
        return CollectionUtility.emptyArrayList();
      }
      return new ArrayList<IBean<T>>(h.getBeans());
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  @Override
  public <T> IBean<T> getBean(Class<T> beanClazz) {
    IBean<T> result = optBean(beanClazz);
    if (result == null) {
      return Assertions.fail("no instances found for query: {}", beanClazz);
    }
    return result;
  }

  @Override
  public <T> IBean<T> optBean(Class<T> beanClazz) {
    List<IBean<T>> list = querySingle(beanClazz);
    if (list.size() == 1) {
      return list.get(0);
    }

    if (list.isEmpty()) {
      return null;
    }
    else {
      return Assertions.fail("multiple instances found for query: {} {}", beanClazz, list);
    }
  }

  @Override
  public <T> List<IBean<T>> getBeans(Class<T> beanClazz) {
    return queryAll(beanClazz);
  }

  @Internal
  protected void setBeanDecorationFactory(IBeanDecorationFactory f) {
    m_beanDecorationFactory = f;
  }

  @Internal
  protected IBeanDecorationFactory getBeanDecorationFactory() {
    return m_beanDecorationFactory;
  }

  public void startCreateImmediatelyBeans() {
    for (IBean bean : getBeans(Object.class)) {
      if (BeanManagerImplementor.isCreateImmediately(bean)) {
        if (BeanManagerImplementor.isApplicationScoped(bean)) {
          bean.getInstance();
        }
        else {
          throw new InitializationException(String.format(
              "Bean '%s' is marked with @%s and is not application scoped (@%s) - unexpected configuration! ",
              bean.getBeanClazz(),
              CreateImmediately.class.getSimpleName(),
              ApplicationScoped.class.getSimpleName()));
        }
      }
    }
  }

  public static boolean isCreateImmediately(IBean<?> bean) {
    return bean.getBeanAnnotation(CreateImmediately.class) != null;
  }

  public static boolean isApplicationScoped(IBean<?> bean) {
    return bean.getBeanAnnotation(ApplicationScoped.class) != null;
  }
}
