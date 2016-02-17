/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.CompileCssWithLess;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyCssWithYui;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.impl.MinifyJsWithYui;
import org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader.SandboxClassLoaderBuilder;

/**
 * Default wrapper for YUI and LESS used to compile and minify javscript and css.
 */
public class ScriptProcessor implements AutoCloseable {
  private URLClassLoader m_yuiLoader;
  private URLClassLoader m_lessLoader;

  public ScriptProcessor() {
    //set up an external private class loader
    m_yuiLoader = new SandboxClassLoaderBuilder()
        .addLocalJar("private-libs/yuicompressor.jar")
        .addJarContaining(ScriptProcessor.class)
        .build(null);
    m_lessLoader = new SandboxClassLoaderBuilder()
        .addLocalJar("private-libs/slf4j-api.jar")
        .addLocalJar("private-libs/jcl-over-slf4j.jar")
        .addLocalJar("private-libs/rhino.jar")
        .addLocalJar("private-libs/lesscss-engine.jar")
        .addJarContaining(ScriptProcessor.class)
        .build(null);
  }

  @Override
  public void close() throws IOException {
    if (m_yuiLoader != null) {
      m_yuiLoader.close();
    }
    if (m_lessLoader != null) {
      m_lessLoader.close();
    }
  }

  public String compileCss(String content) throws IOException {
    return runInClassLoader(m_lessLoader, CompileCssWithLess.class.getName(), content);
  }

  public String compileJs(String content) throws IOException {
    return content;
  }

  public String minifyCss(String content) throws IOException {
    // Work around YUI bug: https://github.com/yui/yuicompressor/issues/59
    // 1. Protect whitespace inside calc() expressions
    Pattern p = Pattern.compile("calc\\s*\\(\\s*(.*?)\\s*\\)");
    Matcher m = p.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      String s = "calc(" + m.group(1).replaceAll("\\s+", "___YUICSSMIN_SPACE_IN_CALC___") + ")";
      m.appendReplacement(sb, s);
    }
    m.appendTail(sb);
    content = sb.toString();
    sb = null; // free memory early

    // 2. Run YUI compressor
    content = runInClassLoader(m_yuiLoader, MinifyCssWithYui.class.getName(), content);

    // 3. Restore protected whitespace
    content = content.replaceAll("___YUICSSMIN_SPACE_IN_CALC___", " ");

    return content;
  }

  public String minifyJs(String content) throws IOException {
    return runInClassLoader(m_yuiLoader, MinifyJsWithYui.class.getName(), content);
  }

  protected String runInClassLoader(ClassLoader loader, String classname, String arg0) throws IOException {
    try {
      Class<?> c = loader.loadClass(classname);
      Object o = c.newInstance();
      Method m = c.getMethod("run", String.class);
      Object result = m.invoke(o, arg0);
      return (String) result;
    }
    catch (InvocationTargetException e0) {
      Throwable t = e0.getTargetException();
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      throw new IOException("Failed running " + classname, e0);
    }
    catch (Exception e1) {
      throw new IOException("Failed running " + classname, e1);
    }
  }
}
