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
__include("jquery/jquery-scout.js");
// protects $ and undefined from being redefined by another library
(function(scout, $, undefined) {
__include("scout/main.js");
__include("scout/objectFactories.js");
__include("scout/widget/Widget.js");
__include("scout/widget/NullWidget.js");
// Basic utilities
__include("scout/util/arrays.js");
__include("scout/util/BoxButtons.js");
__include("scout/util/dates.js");
__include("scout/util/defaultValues.js");
__include("scout/util/DetachHelper.js");
__include("scout/util/Device.js");
__include("scout/util/DoubleClickSupport.js");
__include("scout/util/dragAndDrop.js");
__include("scout/util/EventSupport.js");
__include("scout/util/fonts.js");
__include("scout/util/icons.js");
__include("scout/util/FocusManager.js");
__include("scout/util/FocusContext.js");
__include("scout/util/focusUtils.js");
__include("scout/util/focusRule.js");
__include("scout/util/inspector.js");
__include("scout/util/logging.js");
__include("scout/util/mimeTypes.js");
__include("scout/util/ModelAdapter.js");
__include("scout/util/numbers.js");
__include("scout/util/ObjectFactory.js");
__include("scout/util/objects.js");
__include("scout/util/polyfills.js");
__include("scout/util/Range.js");
__include("scout/util/status.js");
__include("scout/util/strings.js");
__include("scout/util/styles.js");
__include("scout/util/URL.js");
__include("scout/util/GlassPaneRenderer.js");
__include("scout/util/filters.js");
__include("scout/util/hAlign.js");
__include("scout/util/aggregation.js");
// Session related
__include("scout/session/BackgroundJobPollingSupport.js");
__include("scout/session/BusyIndicator.js");
__include("scout/session/Event.js");
__include("scout/session/Locale.js");
__include("scout/session/Reconnector.js");
__include("scout/session/ResponseQueue.js");
__include("scout/session/Session.js");
__include("scout/session/UserAgent.js");
// Basic layout
__include("scout/layout/graphics.js");
__include("scout/layout/AbstractLayout.js");
__include("scout/layout/HtmlComponent.js");
__include("scout/layout/LayoutConstants.js");
__include("scout/layout/LayoutValidator.js");
__include("scout/layout/LogicalGridData.js");
__include("scout/layout/LogicalGridLayout.js");
__include("scout/layout/LogicalGridLayoutInfo.js");
__include("scout/layout/NullLayout.js");
__include("scout/layout/SingleLayout.js");
// Keystroke handling
__include("scout/keystroke/keys.js");
__include("scout/keystroke/Key.js");
__include("scout/keystroke/keyStrokeModifier.js");
__include("scout/keystroke/VirtualKeyStrokeEvent.js");
__include("scout/keystroke/KeyStrokeManager.js");
__include("scout/keystroke/KeyStrokeContext.js");
__include("scout/keystroke/KeyStrokeSupport.js");
__include("scout/keystroke/InputFieldKeyStrokeContext.js");
__include("scout/keystroke/KeyStroke.js");
__include("scout/keystroke/RangeKeyStroke.js");
__include("scout/keystroke/ContextMenuKeyStroke.js");
__include("scout/keystroke/MnemonicKeyStroke.js");
__include("scout/keystroke/ClickActiveElementKeyStroke.js");
__include("scout/keystroke/FocusAdjacentElementKeyStroke.js");
__include("scout/keystroke/CloseKeyStroke.js");
// Misc. elements
__include("scout/scrollbar/Scrollbar.js");
__include("scout/scrollbar/scrollbars.js");
__include("scout/splitter/Splitter.js");
__include("scout/text/DateFormat.js");
__include("scout/text/DecimalFormat.js");
__include("scout/text/Texts.js");
__include("scout/tooltip/Tooltip.js");
__include("scout/util/tooltips.js");
__include("scout/action/Action.js");
__include("scout/action/ActionKeyStroke.js");
__include("scout/popup/Popup.js");
__include("scout/popup/PopupLayout.js");
__include("scout/popup/PopupWithHead.js");
__include("scout/popup/PopupWithHeadLayout.js");
__include("scout/popup/TouchPopup.js");
__include("scout/popup/TouchPopupLayout.js");
__include("scout/datepicker/DatePicker.js");
__include("scout/datepicker/DatePickerLayout.js");
__include("scout/datepicker/DatePickerPopup.js");
__include("scout/datepicker/DatePickerTouchPopup.js");
__include("scout/menu/menus.js");
__include("scout/menu/Menu.js");
__include("scout/menu/MenuKeyStroke.js");
__include("scout/menu/MenuBar.js");
__include("scout/menu/MenuBarLeftKeyStroke.js");
__include("scout/menu/MenuBarRightKeyStroke.js");
__include("scout/menu/MenuBarLayout.js");
__include("scout/menu/MenuExecKeyStroke.js");
__include("scout/menu/MenuItemsOrder.js");
__include("scout/menu/ContextMenuPopup.js");
__include("scout/menu/menuNavigationKeyStrokes.js");
__include("scout/menu/MenuBarPopup.js");
__include("scout/menu/ButtonAdapterMenu.js");
__include("scout/calendar/Calendar.js");
__include("scout/calendar/CalendarComponent.js");
__include("scout/calendar/CalendarListComponent.js");
__include("scout/calendar/CalendarLayout.js");
__include("scout/calendar/DateRange.js");
__include("scout/calendar/YearPanel.js");
__include("scout/planner/Planner.js");
__include("scout/planner/PlannerHeader.js");
__include("scout/planner/PlannerLayout.js");
__include("scout/planner/PlannerMenuItemsOrder.js");
__include("scout/filechooser/FileChooser.js");
__include("scout/filechooser/FileChooserController.js");
// Table
__include("scout/table/Table.js");
__include("scout/table/TableCube.js");
__include("scout/table/TableFooter.js");
__include("scout/table/TableFooterLayout.js");
__include("scout/table/TableInfoFilterTooltip.js");
__include("scout/table/TableInfoLoadTooltip.js");
__include("scout/table/TableInfoSelectionTooltip.js");
__include("scout/table/TableHeader.js");
__include("scout/table/TableHeaderMenu.js");
__include("scout/table/TableHeaderMenuLayout.js");
__include("scout/table/TableHeaderMenuGroup.js");
__include("scout/table/TableHeaderMenuButton.js");
__include("scout/table/TableLayout.js");
__include("scout/table/TableSelectionHandler.js");
__include("scout/table/TableTooltip.js");
__include("scout/table/columns/comparators.js");
__include("scout/table/columns/Column.js");
__include("scout/table/columns/BeanColumn.js"); // requires Column.js
__include("scout/table/columns/BooleanColumn.js"); // requires Column.js
__include("scout/table/columns/DateColumn.js"); // requires Column.js
__include("scout/table/columns/IconColumn.js"); //requires Column.js
__include("scout/table/columns/NumberColumn.js"); //requires Column.js
__include("scout/table/controls/TableControl.js");
__include("scout/table/controls/AggregateTableControl.js"); // requires TableControl.js
__include("scout/table/editor/CellEditorPopup.js");
__include("scout/table/editor/CellEditorPopupLayout.js");
__include("scout/table/editor/CellEditorCancelEditKeyStroke.js");
__include("scout/table/editor/CellEditorCompleteEditKeyStroke.js");
__include("scout/table/editor/CellEditorTabKeyStroke.js");
__include("scout/table/keystrokes/AbstractTableNavigationKeyStroke.js");
__include("scout/table/keystrokes/TableControlCloseKeyStroke.js");
__include("scout/table/keystrokes/TableCopyKeyStroke.js");
__include("scout/table/keystrokes/TableSelectAllKeyStroke.js");
__include("scout/table/keystrokes/TableStartCellEditKeyStroke.js");
__include("scout/table/keystrokes/TableRefreshKeyStroke.js");
__include("scout/table/keystrokes/TableToggleRowKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationUpKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationDownKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationHomeKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationEndKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationPageUpKeyStroke.js");
__include("scout/table/keystrokes/TableNavigationPageDownKeyStroke.js");
__include("scout/table/keystrokes/TableFocusFilterFieldKeyStroke.js");
__include("scout/table/userfilter/TableUserFilter.js");
__include("scout/table/userfilter/ColumnUserFilter.js");
__include("scout/table/userfilter/DateColumnUserFilter.js");
__include("scout/table/userfilter/NumberColumnUserFilter.js");
__include("scout/table/userfilter/TextColumnUserFilter.js");
__include("scout/table/userfilter/TableTextUserFilter.js");
// Tree
__include("scout/tree/Tree.js");
__include("scout/tree/TreeLayout.js");
__include("scout/tree/keystrokes/AbstractTreeNavigationKeyStroke.js");
__include("scout/tree/keystrokes/TreeSpaceKeyStroke.js");
__include("scout/tree/keystrokes/TreeNavigationUpKeyStroke.js");
__include("scout/tree/keystrokes/TreeNavigationDownKeyStroke.js");
__include("scout/tree/keystrokes/TreeCollapseAllKeyStroke.js");
__include("scout/tree/keystrokes/TreeCollapseOrDrillUpKeyStroke.js");
__include("scout/tree/keystrokes/TreeExpandOrDrillDownKeyStroke.js");
// Compact Tree
__include("scout/tree/CompactTree.js");
__include("scout/tree/keystrokes/AbstractCompactTreeControlKeyStroke.js");
__include("scout/tree/keystrokes/CompactTreeUpKeyStroke.js");
__include("scout/tree/keystrokes/CompactTreeDownKeyStroke.js");
__include("scout/tree/keystrokes/CompactTreeLeftKeyStroke.js");
__include("scout/tree/keystrokes/CompactTreeRightKeyStroke.js");
// Desktop
__include("scout/desktop/BaseDesktop.js");
__include("scout/desktop/Desktop.js");
__include("scout/desktop/DesktopTaskBarLayout.js");
__include("scout/desktop/DesktopViewTab.js");
__include("scout/desktop/ViewMenuOpenKeyStroke.js");
__include("scout/desktop/ViewMenuPopup.js");
__include("scout/desktop/ViewMenuTab.js");
__include("scout/desktop/ViewTabSelectKeyStroke.js");
__include("scout/desktop/DisableBrowserTabSwitchingKeyStroke.js");
__include("scout/desktop/DesktopNavigation.js");
__include("scout/desktop/DetailTableTreeFilter.js");
__include("scout/desktop/NullDesktopNavigation.js");
__include("scout/desktop/PopupBlockerHandler.js");
__include("scout/desktop/PopupWindow.js");
__include("scout/desktop/ViewButton.js");
__include("scout/desktop/ViewButtonsLayout.js");
__include("scout/desktop/ViewTabsController.js");
__include("scout/desktop/outline/Outline.js");
__include("scout/desktop/outline/OutlineOverview.js");
__include("scout/desktop/outline/OutlineKeyStrokeContext.js");
__include("scout/desktop/outline/OutlineLayout.js");
__include("scout/desktop/outline/AbstractOutlineNavigationKeyStroke.js");
__include("scout/desktop/outline/OutlineNavigationUpKeyStroke.js");
__include("scout/desktop/outline/OutlineNavigateToTopKeyStroke.js");
__include("scout/desktop/outline/OutlineCollapseOrDrillUpKeyStroke.js");
__include("scout/desktop/outline/OutlineExpandOrDrillDownKeyStroke.js");
__include("scout/desktop/outline/OutlineNavigationDownKeyStroke.js");
__include("scout/desktop/outline/OutlineViewButton.js");
__include("scout/desktop/outline/SearchOutline.js");
__include("scout/desktop/outline/SearchOutlineLayout.js");
// Basics for form fields
__include("scout/form/Form.js");
__include("scout/form/FormLayout.js");
__include("scout/form/FormToolButton.js");
__include("scout/form/FormToolPopup.js");
__include("scout/form/FormToolPopupLayout.js");
__include("scout/form/DialogLayout.js");
__include("scout/form/fields/fields.js");
__include("scout/form/fields/AppLinkKeyStroke.js");
__include("scout/form/fields/DefaultFieldLoadingSupport.js");
__include("scout/form/fields/FormField.js");
__include("scout/form/fields/FormFieldLayout.js");
__include("scout/form/fields/CompositeField.js");
__include("scout/form/fields/ValueField.js");
__include("scout/form/fields/BasicField.js");
__include("scout/form/FormController.js");
// Basics for message boxes
__include("scout/messagebox/MessageBox.js");
__include("scout/messagebox/MessageBoxController.js");
// Form fields (A-Z)
__include("scout/form/fields/beanfield/BeanField.js");
__include("scout/form/fields/browserfield/BrowserField.js");
__include("scout/form/fields/button/Button.js");
__include("scout/form/fields/button/ButtonLayout.js");
__include("scout/form/fields/button/ButtonMnemonicKeyStroke.js");
__include("scout/form/fields/button/ButtonKeyStroke.js");
__include("scout/form/fields/calendarfield/CalendarField.js");
__include("scout/form/fields/checkbox/CheckBoxField.js");
__include("scout/form/fields/checkbox/CheckBoxToggleKeyStroke.js");
__include("scout/form/fields/clipboardfield/ClipboardField.js");
__include("scout/form/fields/colorfield/ColorField.js");
__include("scout/form/fields/datefield/DateField.js");
__include("scout/form/fields/datefield/DateTimeCompositeLayout.js");
__include("scout/form/fields/filechooserfield/FileChooserField.js");
__include("scout/form/fields/groupbox/GroupBox.js");
__include("scout/form/fields/groupbox/GroupBoxLayout.js");
__include("scout/form/fields/groupbox/GroupBoxMenuItemsOrder.js");
__include("scout/form/fields/htmlfield/HtmlField.js");
__include("scout/form/fields/imagefield/ImageField.js");
__include("scout/form/fields/imagefield/ImageFieldLayout.js");
__include("scout/form/fields/labelfield/LabelField.js");
__include("scout/form/fields/listbox/ListBox.js");
__include("scout/form/fields/listbox/ListBoxLayout.js");
__include("scout/form/fields/numberfield/NumberField.js");
__include("scout/form/fields/placeholder/PlaceholderField.js");
__include("scout/form/fields/plannerfield/PlannerField.js");
__include("scout/form/fields/radiobutton/RadioButton.js");
__include("scout/form/fields/radiobutton/RadioButtonGroup.js");
__include("scout/form/fields/radiobutton/RadioButtonGroupLeftKeyStroke.js");
__include("scout/form/fields/radiobutton/RadioButtonGroupRightKeyStroke.js");
__include("scout/form/fields/sequencebox/SequenceBox.js");
__include("scout/form/fields/smartfield/SmartField.js");
__include("scout/form/fields/smartfield/SmartFieldTouchPopup.js");
__include("scout/form/fields/smartfield/SmartFieldLayout.js");
__include("scout/form/fields/smartfield/SmartFieldPopup.js");
__include("scout/form/fields/smartfield/SmartFieldPopupLayout.js");
__include("scout/form/fields/smartfield/ProposalChooser.js");
__include("scout/form/fields/smartfield/ProposalChooserLayout.js");
__include("scout/form/fields/smartfield/SmartFieldMultiline.js");
__include("scout/form/fields/smartfield/SmartFieldMultilineLayout.js");
__include("scout/form/fields/splitbox/SplitBox.js");
__include("scout/form/fields/splitbox/SplitBoxLayout.js");
__include("scout/form/fields/stringfield/StringField.js");
__include("scout/form/fields/stringfield/StringFieldEnterKeyStroke.js");
__include("scout/form/fields/stringfield/StringFieldCtrlEnterKeyStroke.js");
__include("scout/form/fields/tabbox/TabAreaLayout.js");
__include("scout/form/fields/tabbox/TabBox.js");
__include("scout/form/fields/tabbox/TabItemMnemonicKeyStroke.js");
__include("scout/form/fields/tabbox/TabBoxLayout.js");
__include("scout/form/fields/tabbox/TabItem.js");
__include("scout/form/fields/tabbox/TabItemLayout.js");
__include("scout/form/fields/tablefield/TableField.js");
__include("scout/form/fields/treebox/TreeBox.js");
__include("scout/form/fields/treebox/TreeBoxLayout.js");
__include("scout/form/fields/treefield/TreeField.js");
__include("scout/form/fields/wizard/WizardProgressField.js");
__include("scout/form/fields/wizard/WizardProgressFieldLayout.js");
__include("scout/form/fields/wrappedform/WrappedFormField.js");
// More misc. elements
__include("scout/desktop/DesktopFormController.js");
__include("scout/desktop/AbstractNavigateMenu.js"); // requires Menu.js
__include("scout/desktop/NavigateDownMenu.js");
__include("scout/desktop/NavigateUpMenu.js");
__include("scout/table/FilterFieldsGroupBox.js"); // requires GroupBox.js

__include("scout/table/MobileTable.js");
__include("scout/desktop/MobileDesktop.js");
}(window.scout = window.scout || {}, jQuery));
