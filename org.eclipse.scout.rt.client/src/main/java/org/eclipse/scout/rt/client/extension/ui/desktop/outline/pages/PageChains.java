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
package org.eclipse.scout.rt.client.extension.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.ITreeNodeExtension;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.client.ui.desktop.outline.pages.AbstractPage;
import org.eclipse.scout.rt.shared.extension.AbstractExtensionChain;

public final class PageChains {

  private PageChains() {
  }

  protected abstract static class AbstractPageChain extends AbstractExtensionChain<IPageExtension<? extends AbstractPage>> {

    public AbstractPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions, IPageExtension.class);
    }
  }

  public static class PagePageDataLoadedChain extends AbstractPageChain {

    public PagePageDataLoadedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDataLoaded() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageDataLoaded(PagePageDataLoadedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PagePageActivatedChain extends AbstractPageChain {

    public PagePageActivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageActivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageActivated(PagePageActivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageDataChangedChain extends AbstractPageChain {

    public PageDataChangedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDataChanged(final Object... dataTypes) {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDataChanged(PageDataChangedChain.this, dataTypes);
        }
      };
      callChain(methodInvocation, dataTypes);
    }
  }

  public static class PageInitPageChain extends AbstractPageChain {

    public PageInitPageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitPage() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execInitPage(PageInitPageChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageInitDetailFormChain extends AbstractPageChain {

    public PageInitDetailFormChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execInitDetailForm() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execInitDetailForm(PageInitDetailFormChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PagePageDeactivatedChain extends AbstractPageChain {

    public PagePageDeactivatedChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execPageDeactivated() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execPageDeactivated(PagePageDeactivatedChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

  public static class PageDisposePageChain extends AbstractPageChain {

    public PageDisposePageChain(List<? extends ITreeNodeExtension<? extends AbstractTreeNode>> extensions) {
      super(extensions);
    }

    public void execDisposePage() {
      MethodInvocation<Object> methodInvocation = new MethodInvocation<Object>() {
        @Override
        protected void callMethod(IPageExtension<? extends AbstractPage> next) {
          next.execDisposePage(PageDisposePageChain.this);
        }
      };
      callChain(methodInvocation);
    }
  }

}
