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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.testenvironment.TestEnvironmentClientSession;
import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.services.common.jdbc.SearchFilter;
import org.eclipse.scout.rt.testing.client.runner.ClientTestRunner;
import org.eclipse.scout.rt.testing.client.runner.RunWithClientSession;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the reload of a page when multiple summary columns are present. Should not produce an error.
 */
@RunWith(ClientTestRunner.class)
@RunWithSubject("default")
@RunWithClientSession(TestEnvironmentClientSession.class)
public class PageWithTable1Test {

  @Test
  public void testReloadPage_multipleSummaryColumns() throws Exception {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    assertNotNull(desktop);

    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();

    IOutline outline = desktop.getOutline();
    assertNotNull(outline);
    assertSame(PageWithTableOutline.class, outline.getClass());

    IPage<?> page = outline.getActivePage();
    assertNotNull(page);
    assertSame(PageWithTable.class, page.getClass());

    page.reloadPage();
    page.reloadPage();
    page.reloadPage();
  }

  /**
   * Tests that {@link AbstractPage#execPageDataLoaded()} is called correctly on a {@link AbstractTablePage}
   */
  @Test
  public void testExecPageDataLoaded() {
    IDesktop desktop = TestEnvironmentClientSession.get().getDesktop();
    desktop.setAvailableOutlines(Collections.singletonList(new PageWithTableOutline()));
    desktop.setOutline(PageWithTableOutline.class);
    desktop.activateFirstPage();
    IOutline outline = desktop.getOutline();
    PageWithTable page = (PageWithTable) outline.getActivePage();
    Assert.assertEquals(1, page.m_execPageDataLoadedCalled);
    page.reloadPage();
    page.reloadPage();
    page.reloadPage();
    Assert.assertEquals(4, page.m_execPageDataLoadedCalled);
  }

  public static class PageWithTableOutline extends AbstractOutline {

    @Override
    protected void execCreateChildPages(List<IPage<?>> pageList) {
      pageList.add(new PageWithTable());
    }
  }

  public static class PageWithTable extends AbstractPageWithTable<PageWithTable.Table> {

    public int m_execPageDataLoadedCalled = 0;

    @Override
    protected void execLoadData(SearchFilter filter) {
      importTableData(new Object[][]{new Object[]{"a", "b"}});
    }

    @Override
    protected void execPageDataLoaded() {
      super.execPageDataLoaded();
      m_execPageDataLoadedCalled++;
    }

    @Override
    protected IPage<?> execCreateChildPage(ITableRow row) {
      return new PageWithNode();
    }

    public class Table extends AbstractTable {
      @Order(10)
      public class FirstColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredSummary() {
          return true;
        }
      }

      @Order(20)
      public class SecondColumn extends AbstractStringColumn {
        @Override
        protected boolean getConfiguredSummary() {
          return true;
        }
      }
    }
  }

  public static class PageWithNode extends AbstractPageWithNodes {

  }
}
