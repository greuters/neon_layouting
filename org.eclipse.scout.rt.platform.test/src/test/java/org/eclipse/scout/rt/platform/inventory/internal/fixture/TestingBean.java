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
package org.eclipse.scout.rt.platform.inventory.internal.fixture;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.Bean;

@Bean
public class TestingBean {
  @Bean
  public static class S1 {
  }

  @ApplicationScoped
  protected static class S2 {
  }

  @Bean
  private static class S3 {
  }

  @Bean
  static class S4 {
  }

  //must inherit @Bean
  public static class S1Sub1 extends S1 {
  }

  @Bean
  public class M1 {
  }

  @Bean
  public interface I1 {
  }

  @Bean
  interface I2 {
  }

  @Bean
  public enum E1 {
  }

  @Bean
  public @interface A1 {
  }
}
