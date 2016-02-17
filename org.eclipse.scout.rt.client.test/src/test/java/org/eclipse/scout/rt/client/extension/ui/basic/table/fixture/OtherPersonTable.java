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
package org.eclipse.scout.rt.client.extension.ui.basic.table.fixture;

import org.eclipse.scout.rt.client.ui.basic.table.columns.AbstractStringColumn;
import org.eclipse.scout.rt.platform.Order;

public class OtherPersonTable extends AbstractPersonTable {

  public PhoneNumberColumn getPhoneNumberColumn() {
    return getColumnSet().getColumnByClass(PhoneNumberColumn.class);
  }

  @Order(30)
  public class PhoneNumberColumn extends AbstractStringColumn {
  }
}
