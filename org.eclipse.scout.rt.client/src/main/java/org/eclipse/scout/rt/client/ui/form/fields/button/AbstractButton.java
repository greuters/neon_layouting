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
package org.eclipse.scout.rt.client.ui.form.fields.button;

import java.util.EventListener;
import java.util.List;

import org.eclipse.scout.rt.client.ModelContextProxy;
import org.eclipse.scout.rt.client.ModelContextProxy.ModelContext;
import org.eclipse.scout.rt.client.extension.ui.action.tree.MoveActionNodesHandler;
import org.eclipse.scout.rt.client.extension.ui.form.fields.IFormFieldExtension;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonClickActionChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.ButtonChains.ButtonSelectionChangedChain;
import org.eclipse.scout.rt.client.extension.ui.form.fields.button.IButtonExtension;
import org.eclipse.scout.rt.client.services.common.icon.IIconProviderService;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.FormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.AbstractFormField;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.annotations.ConfigOperation;
import org.eclipse.scout.rt.platform.annotations.ConfigProperty;
import org.eclipse.scout.rt.platform.classid.ClassId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.reflect.ConfigurationUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.collection.OrderedCollection;
import org.eclipse.scout.rt.platform.util.concurrent.OptimisticLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ClassId("998788cf-df0f-480b-bd5a-5037805610c9")
public abstract class AbstractButton extends AbstractFormField implements IButton {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractButton.class);
  private final EventListenerList m_listenerList = new EventListenerList();
  private int m_systemType;
  private int m_displayStyle;
  private boolean m_processButton;
  private final IButtonUIFacade m_uiFacade;
  private final OptimisticLock m_uiFacadeSetSelectedLock;

  public AbstractButton() {
    this(true);
  }

  public AbstractButton(boolean callInitializer) {
    super(false);
    m_uiFacade = BEANS.get(ModelContextProxy.class).newProxy(new P_UIFacade(), ModelContext.copyCurrent());
    m_uiFacadeSetSelectedLock = new OptimisticLock();
    if (callInitializer) {
      callInitializer();
    }
  }

  /*
   * Configuration
   */
  /**
   * Configures the system type of this button. See {@code IButton.SYSTEM_TYPE_* } constants for valid values. System
   * buttons are buttons with pre-defined behavior (such as an 'Ok' button or a 'Cancel' button).
   * <p>
   * Subclasses can override this method. Default is {@code IButton.SYSTEM_TYPE_NONE}.
   *
   * @return the system type for a system button, or {@code IButton.SYSTEM_TYPE_NONE} for a non-system button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_SYSTEM_TYPE)
  @Order(200)
  protected int getConfiguredSystemType() {
    return SYSTEM_TYPE_NONE;
  }

  /**
   * Configures whether this button is a process button. Process buttons are typically displayed on a dedicated button
   * bar at the bottom of a form. Non-process buttons can be placed anywhere on a form.
   * <p>
   * Subclasses can override this method. Default is {@code true}.
   *
   * @return {@code true} if this button is a process button, {@code false} otherwise
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BOOLEAN)
  @Order(220)
  protected boolean getConfiguredProcessButton() {
    return true;
  }

  /**
   * Use IKeyStroke constants to define a key stroke for Button execution.
   */
  @ConfigProperty(ConfigProperty.STRING)
  @Order(230)
  protected String getConfiguredKeyStroke() {
    return null;
  }

  /**
   * Configures the display style of this button. See {@code IButton.DISPLAY_STYLE_* } constants for valid values.
   * <p>
   * Subclasses can override this method. Default is {@code IButton.DISPLAY_STYLE_DEFAULT}.
   *
   * @return the display style of this button
   * @see IButton
   */
  @ConfigProperty(ConfigProperty.BUTTON_DISPLAY_STYLE)
  @Order(210)
  protected int getConfiguredDisplayStyle() {
    return DISPLAY_STYLE_DEFAULT;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should only take as much place as is needed to
   * display the button label, but not necessarily more.
   */
  @Override
  protected boolean getConfiguredFillHorizontal() {
    return false;
  }

  /**
   * {@inheritDoc} Default for buttons is false because they usually should use the whole width set by grid layout.
   */
  @Override
  protected boolean getConfiguredGridUseUiWidth() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected boolean getConfiguredGridUseUiHeight() {
    return false;
  }

  /**
   * Configures the icon for this button. The icon is displayed on the button itself. Depending on UI and look and feel,
   * this button might support icon groups to represent different states of this button, such as enabled, disabled,
   * mouse-over etc.
   * <p>
   * Subclasses can override this method. Default is {@code null}.
   *
   * @return the ID (name) of the icon
   * @see IIconGroup
   * @see IIconProviderService
   */
  @ConfigProperty(ConfigProperty.ICON_ID)
  @Order(190)
  protected String getConfiguredIconId() {
    return null;
  }

  /**
   * Configures whether or not the space for the status is visible.
   * <p>
   * Default for buttons is false, because they normally don't fill the grid cell and therefore it is not necessary to
   * align their status with the status of other fields. This makes sure the space next to the button is not wasted by
   * an invisible status.
   */
  @Override
  protected boolean getConfiguredStatusVisible() {
    return false;
  }

  /**
   * Called whenever this button is clicked. This button is disabled and cannot be clicked again until this method
   * returns.
   * <p>
   * Subclasses can override this method. The default does nothing.
   */
  @ConfigOperation
  @Order(190)
  protected void execClickAction() {
  }

  /**
   * Called whenever the selection (of toggle-button) is changed.
   *
   * @param selection
   *          the new selection state
   */
  @ConfigOperation
  @Order(210)
  protected void execSelectionChanged(boolean selection) {
  }

  protected List<Class<? extends IMenu>> getDeclaredMenus() {
    Class[] dca = ConfigurationUtility.getDeclaredPublicClasses(getClass());
    List<Class<IMenu>> menuClasses = ConfigurationUtility.filterClasses(dca, IMenu.class);
    List<Class<? extends IMenu>> a = ConfigurationUtility.removeReplacedClasses(menuClasses);
    return a;
  }

  @Override
  protected void initConfig() {
    super.initConfig();
    setSystemType(getConfiguredSystemType());
    setDisplayStyleInternal(getConfiguredDisplayStyle());
    setProcessButton(getConfiguredProcessButton());
    setIconId(getConfiguredIconId());
    setKeyStroke(getConfiguredKeyStroke());

    // menus
    List<Class<? extends IMenu>> declaredMenus = getDeclaredMenus();
    List<IMenu> contributedMenus = m_contributionHolder.getContributionsByClass(IMenu.class);
    OrderedCollection<IMenu> menus = new OrderedCollection<IMenu>();
    for (Class<? extends IMenu> menuClazz : declaredMenus) {
      IMenu menu;
      menu = ConfigurationUtility.newInnerInstance(this, menuClazz);
      menus.addOrdered(menu);
    }
    menus.addAllOrdered(contributedMenus);
    injectMenusInternal(menus);
    new MoveActionNodesHandler<IMenu>(menus).moveModelObjects();
    IContextMenu contextMenu = new FormFieldContextMenu<IButton>(this, menus.getOrderedList());
    contextMenu.setContainerInternal(this);
    setContextMenu(contextMenu);
  }

  @Override
  protected void initFieldInternal() {
    super.initFieldInternal();
    // init actions
    ActionUtility.initActions(getMenus());
  }

  /**
   * Override this internal method only in order to make use of dynamic menus<br>
   * Used to add and/or remove menus<br>
   * To change the order or specify the insert position use {@link IMenu#setOrder(double)}.
   *
   * @param menus
   *          live and mutable collection of configured menus
   */
  protected void injectMenusInternal(OrderedCollection<IMenu> menus) {
  }

  /*
   * Properties
   */
  @Override
  public String getIconId() {
    return propertySupport.getPropertyString(PROP_ICON_ID);
  }

  @Override
  public void setIconId(String iconId) {
    propertySupport.setPropertyString(PROP_ICON_ID, iconId);
  }

  @Override
  public Object getImage() {
    return propertySupport.getProperty(PROP_IMAGE);
  }

  @Override
  public String getKeyStroke() {
    return propertySupport.getPropertyString(PROP_KEY_STOKE);
  }

  @Override
  public void setKeyStroke(String keyStroke) {
    propertySupport.setPropertyString(PROP_KEY_STOKE, keyStroke);
  }

  @Override
  public void setImage(Object nativeImg) {
    propertySupport.setProperty(PROP_IMAGE, nativeImg);
  }

  @Override
  public int getSystemType() {
    return m_systemType;
  }

  @Override
  public void setSystemType(int systemType) {
    m_systemType = systemType;
  }

  @Override
  public boolean isProcessButton() {
    return m_processButton;
  }

  @Override
  public void setProcessButton(boolean on) {
    m_processButton = on;
  }

  @Override
  public List<IMenu> getMenus() {
    return getContextMenu().getChildActions();
  }

  protected void setContextMenu(IContextMenu contextMenu) {
    propertySupport.setProperty(PROP_CONTEXT_MENU, contextMenu);
  }

  @Override
  public IContextMenu getContextMenu() {
    return (IContextMenu) propertySupport.getProperty(PROP_CONTEXT_MENU);
  }

  @Override
  public void doClick() {
    if (isEnabled() && isVisible() && isEnabledProcessingButton()) {
      try {
        setEnabledProcessingButton(false);

        fireButtonClicked();
        interceptClickAction();
      }
      finally {
        setEnabledProcessingButton(true);
      }
    }
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public boolean isSelected() {
    return propertySupport.getPropertyBool(PROP_SELECTED);
  }

  /**
   * Toggle and Radio Buttons
   */
  @Override
  public void setSelected(boolean b) {
    boolean changed = propertySupport.setPropertyBool(PROP_SELECTED, b);
    // single observer for config
    if (changed) {
      try {
        interceptSelectionChanged(b);
      }
      catch (RuntimeException e) {
        BEANS.get(ExceptionHandler.class).handle(e);
      }
    }
  }

  @Override
  public void disarm() {
    fireDisarmButton();
  }

  @Override
  public void requestPopup() {
    fireRequestPopup();
  }

  @Override
  public int getDisplayStyle() {
    return m_displayStyle;
  }

  @Override
  public void setDisplayStyleInternal(int i) {
    m_displayStyle = i;
  }

  @Override
  public IButtonUIFacade getUIFacade() {
    return m_uiFacade;
  }

  /**
   * Model Observer
   */
  @Override
  public void addButtonListener(ButtonListener listener) {
    m_listenerList.add(ButtonListener.class, listener);
  }

  @Override
  public void removeButtonListener(ButtonListener listener) {
    m_listenerList.remove(ButtonListener.class, listener);
  }

  private void fireButtonClicked() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_CLICKED));
  }

  private void fireDisarmButton() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_DISARM));
  }

  private void fireRequestPopup() {
    fireButtonEvent(new ButtonEvent(this, ButtonEvent.TYPE_REQUEST_POPUP));
  }

  // main handler
  private void fireButtonEvent(ButtonEvent e) {
    EventListener[] listeners = m_listenerList.getListeners(ButtonListener.class);
    if (listeners != null && listeners.length > 0) {
      for (int i = 0; i < listeners.length; i++) {
        ((ButtonListener) listeners[i]).buttonChanged(e);
      }
    }
  }

  @Override
  public void setView(boolean visible, boolean enabled) {
    setVisible(visible);
    setEnabled(enabled);
  }

  @Override
  protected void disposeFieldInternal() {
    super.disposeFieldInternal();
    for (IMenu menu : getMenus()) {
      try {
        menu.dispose();
      }
      catch (RuntimeException e) {
        LOG.warn("Exception while disposing menu.", e);
      }
    }
  }

  /**
   * Default implementation for buttons
   */
  protected class P_UIFacade implements IButtonUIFacade {
    @Override
    public void fireButtonClickedFromUI() {
      if (isEnabled() && isVisible()) {
        doClick();
      }
    }

    /**
     * Toggle and Radio Buttons
     */
    @Override
    public void setSelectedFromUI(boolean b) {
      try {
        if (isEnabled() && isVisible()) {
          // Ticket 76711: added optimistic lock
          if (m_uiFacadeSetSelectedLock.acquire()) {
            setSelected(b);
          }
        }
      }
      finally {
        m_uiFacadeSetSelectedLock.release();
      }
    }
  }

  protected final void interceptSelectionChanged(boolean selection) {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ButtonSelectionChangedChain chain = new ButtonSelectionChangedChain(extensions);
    chain.execSelectionChanged(selection);
  }

  protected final void interceptClickAction() {
    List<? extends IFormFieldExtension<? extends AbstractFormField>> extensions = getAllExtensions();
    ButtonClickActionChain chain = new ButtonClickActionChain(extensions);
    chain.execClickAction();
  }

  protected static class LocalButtonExtension<OWNER extends AbstractButton> extends LocalFormFieldExtension<OWNER> implements IButtonExtension<OWNER> {

    public LocalButtonExtension(OWNER owner) {
      super(owner);
    }

    @Override
    public void execSelectionChanged(ButtonSelectionChangedChain chain, boolean selection) {
      getOwner().execSelectionChanged(selection);
    }

    @Override
    public void execClickAction(ButtonClickActionChain chain) {
      getOwner().execClickAction();
    }
  }

  @Override
  protected IButtonExtension<? extends AbstractButton> createLocalExtension() {
    return new LocalButtonExtension<AbstractButton>(this);
  }
}
