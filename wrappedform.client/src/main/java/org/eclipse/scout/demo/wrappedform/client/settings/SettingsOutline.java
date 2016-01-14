package org.eclipse.scout.demo.wrappedform.client.settings;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.client.ui.desktop.outline.AbstractOutline;
import org.eclipse.scout.rt.shared.TEXTS;

import org.eclipse.scout.demo.wrappedform.shared.Icons;

/**
 * <h3>{@link SettingsOutline}</h3>
 *
 * @author sgr
 */
@Order(3000)
public class SettingsOutline extends AbstractOutline {

  @Override
  protected String getConfiguredTitle() {
    return TEXTS.get("Settings");
  }

  @Override
  protected String getConfiguredIconId() {
    return Icons.Gear;
  }
}
