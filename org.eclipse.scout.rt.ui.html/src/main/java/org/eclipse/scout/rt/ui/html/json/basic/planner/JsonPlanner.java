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
package org.eclipse.scout.rt.ui.html.json.basic.planner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.client.ui.AbstractEventBuffer;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.basic.planner.Activity;
import org.eclipse.scout.rt.client.ui.basic.planner.IPlanner;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerAdapter;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerEvent;
import org.eclipse.scout.rt.client.ui.basic.planner.PlannerListener;
import org.eclipse.scout.rt.client.ui.basic.planner.Resource;
import org.eclipse.scout.rt.platform.util.Range;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiException;
import org.eclipse.scout.rt.ui.html.json.AbstractJsonPropertyObserver;
import org.eclipse.scout.rt.ui.html.json.IIdProvider;
import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;
import org.eclipse.scout.rt.ui.html.json.JsonDate;
import org.eclipse.scout.rt.ui.html.json.JsonDateRange;
import org.eclipse.scout.rt.ui.html.json.JsonEvent;
import org.eclipse.scout.rt.ui.html.json.JsonObjectUtility;
import org.eclipse.scout.rt.ui.html.json.JsonProperty;
import org.eclipse.scout.rt.ui.html.json.action.DisplayableActionFilter;
import org.eclipse.scout.rt.ui.html.json.menu.IJsonContextMenuOwner;
import org.eclipse.scout.rt.ui.html.json.menu.JsonContextMenu;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonPlanner<PLANNER extends IPlanner<?, ?>> extends AbstractJsonPropertyObserver<PLANNER> implements IJsonContextMenuOwner {
  private static final Logger LOG = LoggerFactory.getLogger(JsonPlanner.class);

  // from model
  public static final String EVENT_PLANNER_CHANGED = "plannerChanged";
  public static final String EVENT_RESOURCES_INSERTED = "resourcesInserted";
  public static final String EVENT_RESOURCES_UPDATED = "resourcesUpdated";
  public static final String EVENT_RESOURCES_DELETED = "resourcesDeleted";
  public static final String EVENT_RESOURCES_SELECTED = "resourcesSelected";
  public static final String EVENT_ALL_RESOURCES_DELETED = "allResourcesDeleted";

  // from UI
  private static final String EVENT_SET_DISPLAY_MODE = "setDisplayMode";
  private static final String EVENT_SET_VIEW_RANGE = "setViewRange";
  private static final String EVENT_SET_SELECTION = "setSelection";
  private static final String EVENT_SET_SELECTED_ACTIVITY_CELLS = "setSelectedActivityCells";

  private PlannerListener m_plannerListener;
  private final Map<String, Activity<?, ?>> m_activities;
  private final Map<Activity<?, ?>, String> m_activityIds;
  private final Map<String, Resource<?>> m_resources;
  private final Map<Resource<?>, String> m_resourceIds;
  private final AbstractEventBuffer<PlannerEvent> m_eventBuffer;
  private final PlannerEventFilter m_plannerEventFilter;

  public JsonPlanner(PLANNER model, IUiSession uiSession, String id, IJsonAdapter<?> parent) {
    super(model, uiSession, id, parent);
    m_activities = new HashMap<>();
    m_activityIds = new HashMap<>();
    m_resources = new HashMap<>();
    m_resourceIds = new HashMap<>();
    m_eventBuffer = model.createEventBuffer();
    m_plannerEventFilter = new PlannerEventFilter(this);
  }

  @Override
  public String getObjectType() {
    return "Planner";
  }

  @Override
  protected void attachModel() {
    super.attachModel();
    if (m_plannerListener != null) {
      throw new IllegalStateException();
    }
    m_plannerListener = new P_PlannerListener();
    getModel().addPlannerListener(m_plannerListener);
  }

  @Override
  protected void detachModel() {
    super.detachModel();
    if (m_plannerListener == null) {
      throw new IllegalStateException();
    }
    getModel().removePlannerListener(m_plannerListener);
    m_plannerListener = null;
  }

  @Override
  protected void attachChildAdapters() {
    super.attachChildAdapters();
    attachAdapter(getModel().getContextMenu(), new DisplayableActionFilter<IMenu>());
  }

  @Override
  protected void initJsonProperties(PLANNER model) {
    super.initJsonProperties(model);
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_LABEL, model) {
      @Override
      protected String modelValue() {
        return getModel().getLabel();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_AVAILABLE_DISPLAY_MODES, model) {
      @Override
      protected Set<Integer> modelValue() {
        return getModel().getAvailableDisplayModes();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_DISPLAY_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getDisplayMode();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_VIEW_RANGE, model) {
      @Override
      protected Range<Date> modelValue() {
        return getModel().getViewRange();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return new JsonDateRange((Range<Date>) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_HEADER_VISIBLE, model) {
      @Override
      protected Boolean modelValue() {
        return getModel().isHeaderVisible();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_SELECTION_RANGE, model) {
      @Override
      protected Range<Date> modelValue() {
        return getModel().getSelectionRange();
      }

      @SuppressWarnings("unchecked")
      @Override
      public Object prepareValueForToJson(Object value) {
        if (value == null) {
          return null;
        }
        return new JsonDateRange((Range<Date>) value).toJson();
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_SELECTED_ACTIVITY, model) {
      @Override
      protected Activity<?, ?> modelValue() {
        return getModel().getSelectedActivity();
      }

      @Override
      public Object prepareValueForToJson(Object value) {
        Activity<?, ?> activityCell = (Activity<?, ?>) value;
        return new P_GetOrCreateCellIdProvider().getId(activityCell);
      }
    });
    putJsonProperty(new JsonProperty<PLANNER>(IPlanner.PROP_SELECTION_MODE, model) {
      @Override
      protected Integer modelValue() {
        return getModel().getSelectionMode();
      }
    });

  }

  @Override
  public JSONObject toJson() {
    JSONObject json = super.toJson();
    List<? extends Resource<?>> resources = getModel().getResources();
    JSONArray jsonResources = new JSONArray();
    for (Resource<?> resource : resources) {
      // Can't use NewResourceId Provider because properties come before toJson and already create an id
      Object jsonResource = resourceToJson(resource, new P_GetOrCreateResourceIdProvider(), new P_GetOrCreateCellIdProvider());
      LOG.debug("Id: {}. Resources: {}", getId(), jsonResource);
      jsonResources.put(jsonResource);
    }
    json.put("resources", jsonResources);
    JsonContextMenu<IContextMenu> jsonContextMenu = getAdapter(getModel().getContextMenu());
    if (jsonContextMenu != null) {
      json.put(PROP_MENUS, jsonContextMenu.childActionsToJson());
    }
    putProperty(json, "selectedResources", resourceIdsToJson(getModel().getSelectedResources(), new P_ResourceIdProvider()));
    return json;
  }

  protected String getCellId(Activity<?, ?> cell) {
    return m_activityIds.get(cell);
  }

  protected String createCellId(Activity<?, ?> cell) {
    String id = getCellId(cell);
    if (id != null) {
      throw new IllegalStateException("Cell already has an id. " + cell);
    }
    id = getUiSession().createUniqueId();
    m_activities.put(id, cell);
    m_activityIds.put(cell, id);
    return id;
  }

  protected String getResourceId(Resource<?> resource) {
    if (resource == null) {
      return null;
    }
    return m_resourceIds.get(resource);
  }

  protected Resource getResource(String id) {
    if (id == null) {
      return null;
    }
    return m_resources.get(id);
  }

  protected String getActivityId(Activity<?, ?> activity) {
    if (activity == null) {
      return null;
    }
    return m_activityIds.get(activity);
  }

  protected Activity getActivity(String id) {
    if (id == null) {
      return null;
    }
    return m_activities.get(id);
  }

  protected String createResourceId(Resource<?> resource) {
    String id = getResourceId(resource);
    if (id != null) {
      throw new IllegalStateException("Resource already has an id. " + resource);
    }
    id = getUiSession().createUniqueId();
    m_resources.put(id, resource);
    m_resourceIds.put(resource, id);
    return id;
  }

  protected void disposeResource(Resource<?> resource) {
    String resourceId = getResourceId(resource);
    m_resourceIds.remove(resource);
    m_resources.remove(resourceId);
    for (Activity<?, ?> activity : resource.getActivities()) {
      m_activities.remove(activity);
      m_activityIds.remove(activity.getId());
    }
  }

  protected void disposeAllResources() {
    m_resourceIds.clear();
    m_resources.clear();
    m_activityIds.clear();
    m_activities.clear();
  }

  protected void handleModelEvent(PlannerEvent event) {
    event = m_plannerEventFilter.filter(event);
    if (event == null) {
      return;
    }
    // Add event to buffer instead of handling it immediately. (This allows coalescing the events at JSON response level.)
    m_eventBuffer.add(event);
    registerAsBufferedEventsAdapter();
  }

  @Override
  public void processBufferedEvents() {
    if (m_eventBuffer.isEmpty()) {
      return;
    }
    List<PlannerEvent> coalescedEvents = m_eventBuffer.consumeAndCoalesceEvents();
    for (PlannerEvent event : coalescedEvents) {
      processEvent(event);
    }
  }

  protected void processEvent(PlannerEvent event) {
    switch (event.getType()) {
      case PlannerEvent.TYPE_RESOURCES_INSERTED:
        handleModelResourcesInserted(event.getResources());
        break;
      case PlannerEvent.TYPE_RESOURCES_UPDATED:
        handleModelResourcesUpdated(event.getResources());
        break;
      case PlannerEvent.TYPE_RESOURCES_DELETED:
        handleModelResourcesDeleted(event.getResources());
        break;
      case PlannerEvent.TYPE_RESOURCES_SELECTED:
        handleModelResourcesSelected(event.getResources());
        break;
      case PlannerEvent.TYPE_ALL_RESOURCES_DELETED:
        handleModelAllResourcesDeleted();
        break;
      default:
        // NOP
    }
  }

  protected void handleModelResourcesSelected(List<? extends Resource> resources) {
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, "resourceIds", resourceIdsToJson(resources, new P_ResourceIdProvider()));
    addActionEvent(EVENT_RESOURCES_SELECTED, jsonEvent);
  }

  protected void handleModelResourcesInserted(List<? extends Resource> resources) {
    JSONArray jsonResources = new JSONArray();
    for (Resource resource : resources) {
      Object jsonResource = new JsonResource(resource, new P_NewResourceIdProvider(), new P_NewCellIdProvider()).toJson();
      jsonResources.put(jsonResource);
    }
    if (jsonResources.length() == 0) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("resources", jsonResources);
    addActionEvent(EVENT_RESOURCES_INSERTED, jsonEvent);
  }

  protected void handleModelResourcesUpdated(List<? extends Resource> resources) {
    JSONArray jsonResources = new JSONArray();
    for (Resource resource : resources) {
      Object jsonResource = resourceToJson(resource, new P_GetOrCreateResourceIdProvider(), new P_GetOrCreateCellIdProvider());
      jsonResources.put(jsonResource);
    }
    JSONObject jsonEvent = new JSONObject();
    putProperty(jsonEvent, "resources", jsonResources);
    addActionEvent(EVENT_RESOURCES_UPDATED, jsonEvent);
  }

  protected void handleModelResourcesDeleted(List<? extends Resource> resources) {
    if (resources.isEmpty()) {
      return;
    }
    JSONObject jsonEvent = new JSONObject();
    for (Resource resource : resources) {
      String resourceId = getResourceId(resource);
      jsonEvent.append("resourceIds", resourceId);
      disposeResource(resource);
    }
    addActionEvent(EVENT_RESOURCES_DELETED, jsonEvent);
  }

  protected void handleModelAllResourcesDeleted() {
    disposeAllResources();
    addActionEvent(EVENT_ALL_RESOURCES_DELETED, new JSONObject());
  }

  protected void handleModelResourceOrderChanged(List<? extends Resource> resources) {
    JSONObject jsonEvent = new JSONObject();
    jsonEvent.put("resourceIds", resourceIdsToJson(resources, new P_ResourceIdProvider()));
    JSONArray jsonResourceIds = new JSONArray();
    for (Resource resource : resources) {
      String resourceId = getResourceId(resource);
      jsonResourceIds.put(resourceId);
    }
    if (jsonResourceIds.length() == 0) {
      return;
    }
    addActionEvent("resourceOrderChanged", jsonEvent);
  }

  @Override
  public void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters) {
    addPropertyChangeEvent(PROP_MENUS, JsonObjectUtility.adapterIdsToJson(menuAdapters));
  }

  @Override
  public void handleUiEvent(JsonEvent event) {
    if (EVENT_SET_DISPLAY_MODE.equals(event.getType())) {
      handleUiSetDisplayMode(event);
    }
    else if (EVENT_SET_VIEW_RANGE.equals(event.getType())) {
      handleUiSetViewRange(event);
    }
    else if (EVENT_SET_SELECTION.equals(event.getType())) {
      handleUiSetSelection(event);
    }
    else if (EVENT_SET_SELECTED_ACTIVITY_CELLS.equals(event.getType())) {
      handleUiSetSelectedActivityCells(event);
    }
    else {
      super.handleUiEvent(event);
    }
  }

  protected void handleUiSetDisplayMode(JsonEvent event) {
    JSONObject data = event.getData();
    int displayMode = data.getInt("displayMode");
    addPropertyEventFilterCondition(IPlanner.PROP_DISPLAY_MODE, displayMode);
    getModel().getUIFacade().setDisplayModeFromUI(displayMode);
  }

  @SuppressWarnings("unchecked")
  protected void handleUiSetViewRange(JsonEvent event) {
    JSONObject data = event.getData();
    Range<Date> viewRange = extractViewRange(data);
    addPropertyEventFilterCondition(IPlanner.PROP_VIEW_RANGE, viewRange);
    getModel().getUIFacade().setViewRangeFromUI(viewRange);
  }

  @SuppressWarnings("unchecked")
  protected void handleUiSetSelection(JsonEvent event) {
    Range<Date> selectionRange = extractSelectionRange(event.getData());
    List<Resource<?>> resources = extractResources(event.getData());
    addPlannerEventFilterCondition(PlannerEvent.TYPE_RESOURCES_SELECTED).setResources(resources);
    addPropertyEventFilterCondition(IPlanner.PROP_SELECTION_RANGE, selectionRange);
    getModel().getUIFacade().setSelectionFromUI(resources, selectionRange);
    LOG.debug("selectionRange={}", selectionRange);
  }

  private Range<Date> extractSelectionRange(JSONObject data) {
    JSONObject selectionRange = data.optJSONObject("selectionRange");
    Date fromDate = toJavaDate(selectionRange, "from");
    Date toDate = toJavaDate(selectionRange, "to");
    return new Range<Date>(fromDate, toDate);
  }

  private Range<Date> extractViewRange(JSONObject data) {
    JSONObject range = data.optJSONObject("viewRange");
    Date fromDate = toJavaDate(range, "from");
    Date toDate = toJavaDate(range, "to");
    return new Range<Date>(fromDate, toDate);
  }

  private Date toJavaDate(JSONObject data, String propertyName) {
    return new JsonDate(data.optString(propertyName, null)).asJavaDate();
  }

  @SuppressWarnings("unchecked")
  protected void handleUiSetSelectedActivityCells(JsonEvent event) {
    Activity<?, ?> activityCell = null;
    // FIXME cgu: Map data from JSON

    getModel().getUIFacade().setSelectedActivityCellFromUI(activityCell);
  }

  protected List<Resource<?>> extractResources(JSONObject json) {
    return jsonToResources(json.getJSONArray("resourceIds"));
  }

  protected List<Resource<?>> jsonToResources(JSONArray resourceIds) {
    List<Resource<?>> resources = new ArrayList<>(resourceIds.length());
    for (int i = 0; i < resourceIds.length(); i++) {
      Resource<?> resource = getResource((String) resourceIds.get(i));
      if (resource == null) {
        throw new UiException("No resource found for id " + resourceIds.get(i));
      }
      resources.add(resource);
    }
    return resources;
  }

  protected Object resourceToJson(Resource resource) {
    return new JsonResource(resource, new P_NewResourceIdProvider(), new P_NewCellIdProvider()).toJson();
  }

  protected Object resourceToJson(Resource resource, IIdProvider<Resource<?>> idProvider, IIdProvider<Activity<?, ?>> cellIdProvider) {
    return new JsonResource(resource, idProvider, cellIdProvider).toJson();
  }

  protected JSONArray resourceIdsToJson(List<? extends Resource> resources, IIdProvider<Resource<?>> idProvider) {
    JSONArray jsonResourceIds = new JSONArray();
    for (Resource resource : resources) {
      String resourceId = idProvider.getId(resource);
      if (resourceId == null) { // Ignore resources that are not yet sent to the UI
        continue;
      }
      jsonResourceIds.put(resourceId);
    }
    return jsonResourceIds;
  }

  protected PlannerEventFilterCondition addPlannerEventFilterCondition(int plannerEventType) {
    PlannerEventFilterCondition conditon = new PlannerEventFilterCondition(plannerEventType);
    m_plannerEventFilter.addCondition(conditon);
    return conditon;
  }

  @Override
  public void cleanUpEventFilters() {
    super.cleanUpEventFilters();
    m_plannerEventFilter.removeAllConditions();
  }

  protected class P_PlannerListener extends PlannerAdapter {

    @Override
    public void plannerChanged(PlannerEvent event) {
      handleModelEvent(event);
    }
  }

  protected class P_CellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      return getCellId(cell);
    }
  }

  protected class P_NewCellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      return createCellId(cell);
    }
  }

  protected class P_GetOrCreateCellIdProvider implements IIdProvider<Activity<?, ?>> {

    @Override
    public String getId(Activity<?, ?> cell) {
      if (cell == null) {
        return null;
      }
      String id = getCellId(cell);
      if (id == null) {
        id = createCellId(cell);
      }
      return id;
    }
  }

  protected class P_ResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      return getResourceId(resource);
    }
  }

  protected class P_NewResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      return createResourceId(resource);
    }
  }

  protected class P_GetOrCreateResourceIdProvider implements IIdProvider<Resource<?>> {

    @Override
    public String getId(Resource<?> resource) {
      if (resource == null) {
        return null;
      }
      String id = getResourceId(resource);
      if (id == null) {
        id = createResourceId(resource);
      }
      return id;
    }
  }
}
