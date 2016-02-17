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
package org.eclipse.scout.rt.client.mobile.transformation;

/**
 * @since 3.9.0
 */
public enum MobileDeviceTransformation implements IDeviceTransformation {
  MOVE_FIELD_LABEL_TO_TOP,
  MAKE_FIELD_SCALEABLE,
  MAKE_MAINBOX_SCROLLABLE,
  REDUCE_GROUPBOX_COLUMNS_TO_ONE,
  HIDE_PLACEHOLDER_FIELD,
  DISABLE_FORM_CANCEL_CONFIRMATION,
  DISPLAY_FORM_HEADER,
  ADD_MISSING_BACK_ACTION_TO_FORM_HEADER,
  DISPLAY_OUTLINE_ROOT_NODE,
  DISPLAY_PAGE_TABLE
}
