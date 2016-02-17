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
package org.eclipse.scout.rt.client.ui.basic.table.columns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.scout.rt.client.ui.basic.table.AbstractTable;
import org.eclipse.scout.rt.client.ui.basic.table.ITableRow;
import org.eclipse.scout.rt.client.ui.form.fields.booleanfield.IBooleanField;
import org.eclipse.scout.rt.platform.Order;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AbstractBooleanColumn}
 */
public class AbstractBooleanColumnTest {

  @Test
  public void testPrepareEditInternal() {
    AbstractBooleanColumn column = new AbstractBooleanColumn() {
    };
    column.setMandatory(true);
    ITableRow row = Mockito.mock(ITableRow.class);
    IBooleanField field = (IBooleanField) column.prepareEditInternal(row);
    assertEquals("mandatory property to be progagated to field", column.isMandatory(), field.isMandatory());
  }

  @Test
  public void testNoNullValues() {
    TestTable table = new TestTable();
    table.addRowByArray(new Object[]{null});
    Boolean value = table.getTestBooleanColumn().getValue(0);
    assertNotNull(value);
    assertFalse(value);
  }

  public class TestTable extends AbstractTable {

    public TestBooleanColumn getTestBooleanColumn() {
      return getColumnSet().getColumnByClass(TestBooleanColumn.class);
    }

    @Order(10)
    public class TestBooleanColumn extends AbstractBooleanColumn {
    }

  }

}
