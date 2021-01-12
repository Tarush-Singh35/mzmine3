/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */

package io.github.mzmine.util.javafx.groupablelistview;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.sun.istack.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javax.annotation.Nonnull;

/**
 * Class extending ListView with possibility of items grouping.
 *
 * @param <T> type of the contained items
 */
public class GroupableListView<T> extends ListView<GroupableListViewEntity> {

  private final Map<GroupEntity, ObservableList<ValueEntity<T>>> groups = FXCollections.observableHashMap();
  private final ObservableList<GroupableListViewEntity> items = FXCollections.observableArrayList();

  private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
  private final ObservableList<GroupEntity> selectedGroups = FXCollections.observableArrayList();

  public GroupableListView() {
    setEditable(false);

    // Bind selected values and groups' to selected entities
    getSelectionModel().getSelectedItems().addListener(new ListChangeListener<>() {
      @Override
      public void onChanged(Change<? extends GroupableListViewEntity> change) {
        while (change.next()) {
          if (change.getList() == null) {
            return;
          }
          ImmutableList<GroupableListViewEntity> items = ImmutableList.copyOf(change.getList());
          selectedItems.clear();
          selectedGroups.clear();
          for (GroupableListViewEntity item : items) {
            if (item instanceof GroupEntity) {
              selectedGroups.add((GroupEntity) item);
              selectedItems.addAll(groups.get(item).stream()
                  .map(ValueEntity::getValue)
                  .collect(Collectors.toList()));
            } else {
              if (!selectedItems.contains(((ValueEntity<T>) item).getValue())) {
                selectedItems.add(((ValueEntity<T>) item).getValue());
              }
            }
          }
        }
      }
    });
  }

  /**
   * Binds the values of this {@link GroupableListView} to the given {@link ObservableList}.
   *
   * @param values list to be binded
   */
  public final void setValues(@Nonnull ObservableList<T> values) {
    items.clear();
    groups.clear();

    values.addListener(new ListChangeListener<T>() {
      @Override
      public void onChanged(Change<? extends T> change) {
        while (change.next()) {
          if (change.wasAdded()) {
            change.getAddedSubList().forEach(item -> items.add(new ValueEntity<T>(item)));
          }
          if (change.wasRemoved()) {
            for (T removedItem : change.getRemoved()) {
              for (GroupableListViewEntity item : items) {
                if (item instanceof ValueEntity && ((ValueEntity<?>) item).getValue().equals(removedItem)) {
                  items.remove(item);
                  if (((ValueEntity<?>) item).isGrouped()) {
                    groups.get(((ValueEntity<?>) item).getGroup()).remove(item);
                    if (groups.get(((ValueEntity<?>) item).getGroup()).isEmpty()) {
                      ungroupItems(((ValueEntity<?>) item).getGroup());
                    }
                  }
                  break;
                }
              }
            }
          }
          setItems(items);
        }
      }
    });
  }

  public void groupSelectedItems() {
    List<Integer> selectedIndices = ImmutableList.copyOf(getSelectionModel().getSelectedIndices());
    groupItems(selectedIndices, generateNewGroupName("New group"));

    getSelectionModel().clearSelection();
    int firstGroupIndex = Collections.min(selectedIndices) + 1;
    getSelectionModel().selectRange(firstGroupIndex, firstGroupIndex + selectedIndices.size());

    Platform.runLater(() -> {
      setEditable(true);
      edit(Collections.min(selectedIndices));
    });
  }

  public void groupItems(List<Integer> itemsIndices, String groupName) {
    GroupEntity newGroup = new GroupEntity(groupName);

    ObservableList<ValueEntity<T>> groupItems = itemsIndices.stream()
        .map(index -> (ValueEntity<T>) getItems().get(index))
        .collect(Collectors.toCollection(FXCollections::observableArrayList));
    groupItems.forEach(item -> item.setGroup(newGroup));

    groups.put(newGroup, FXCollections.observableArrayList(groupItems));

    items.removeAll(groupItems);
    int minIndex = Collections.min(itemsIndices);
    items.add(minIndex, newGroup);
    items.addAll(minIndex + 1, groupItems);
  }

  public void ungroupItems(List<GroupEntity> groups) {
    if (groups.equals(selectedGroups)) {
      // Create list copy to avoid concurrent modification of selected groups
      groups = List.copyOf(groups);
    }

    groups.forEach(this::ungroupItems);
  }

  public void ungroupItems(GroupEntity group) {
    groups.get(group).forEach(item -> item.setGroup(null));
    if (group.isHidden()) {
      items.addAll(groups.get(group));
    }

    items.remove(group);
    groups.remove(group);
  }

  public ObservableList<T> getSelectedItems() {
    return selectedItems;
  }

  public ObservableList<GroupEntity> getSelectedGroups() {
    return selectedGroups;
  }

  public void renameGroup(GroupEntity group, String newName) {
    String oldName = group.getGroupName();
    if (!groups.containsKey(group) || oldName.equals(newName)) {
      return;
    }

    // Modify new name to be unique among group names
    newName = generateNewGroupName(newName);

    group.setGroupName(newName);
  }

  public ObservableList<ValueEntity<T>> getGroupItems(GroupEntity group) {
    return groups.get(group);
  }

  public boolean onlyGroupsSelected() {
    for (GroupableListViewEntity selectedItem : getSelectionModel().getSelectedItems()) {
      if (!(selectedItem instanceof GroupEntity)) {
        return false;
      }
    }
    return true;
  }

  public boolean onlyGroupedItemsSelected() {
    for (GroupableListViewEntity selectedItem : getSelectionModel().getSelectedItems()) {
      if (!(selectedItem instanceof ValueEntity && ((ValueEntity<?>) selectedItem).isGrouped())) {
        return false;
      }
    }
    return true;
  }

  public boolean onlyItemsSelected() {
    return selectedGroups.isEmpty() && !selectedItems.isEmpty();
  }

  public boolean anyGroupedItemSelected() {
    for (GroupableListViewEntity selectedItem : getSelectionModel().getSelectedItems()) {
      if (selectedItem instanceof ValueEntity && ((ValueEntity<?>) selectedItem).isGrouped()) {
        return true;
      }
    }
    return false;
  }


  public boolean anyGroupSelected() {
    return !selectedGroups.isEmpty();
  }

  public void addToGroup(GroupEntity group, int index, List<ValueEntity<T>> items) {
    if (group == null || !groups.containsKey(group)) {
      return;
    }

    items.forEach(item -> item.setGroup(group));
    groups.get(group).addAll(index, items);
  }

  public void addToGroup(GroupEntity group, int index, ValueEntity<T> item) {
    addToGroup(group, index, List.of(item));
  }

  public void removeValuesFromGroup(List<T> values) {
    if (values.equals(selectedItems)) {
      // Create list copy to avoid concurrent modification of selected items
      values = List.copyOf(values);
    }

    // Remove values' entities from the list and place them to the end
    values.forEach(value -> removeFromGroup(items.size() - 1, getValueEntity(value)));

    // Select ungrouped items
    getSelectionModel().clearSelection();
    getSelectionModel().selectRange(items.size() - values.size(), items.size());
  }

  /**
   * Removes item from it's group and places it to the given index of the list view.
   *
   * @param index new index of the item
   * @param item item to remove from it's group
   */
  public void removeFromGroup(int index, ValueEntity<T> item) {
    if (item == null || !item.isGrouped()) {
      return;
    }

    removeFromGroup(item);
    items.remove(item);
    // Compensate removed item
    if (items.indexOf(item) > index) {
      index++;
    }
    items.add(index, item);

    getSelectionModel().clearAndSelect(index);
  }

  public void removeFromGroup(ValueEntity<T> item) {
    if (item == null || !item.isGrouped()) {
      return;
    }

    GroupEntity group = item.getGroup();

    item.setGroup(null);
    groups.get(group).remove(item);

    if (groups.get(group).isEmpty()) {
      ungroupItems(group);
    }
  }

  public Integer getGroupIndex(GroupEntity group) {
    if (group == null || !groups.containsKey(group)) {
      return null;
    }

    for (int index = 0; index < items.size(); index++) {
      if (items.get(index) instanceof GroupEntity
          && (items.get(index)).equals(group)) {
        return index;
      }
    }
    return null;
  }

  public List<Integer> getGroupItemsIndices(GroupEntity group) {
    Integer groupIndex = getGroupIndex(group);
    return IntStream.rangeClosed(groupIndex + 1, groupIndex + getGroupSize(group))
        .boxed().collect(Collectors.toList());
  }

  public Integer getGroupSize(GroupEntity group) {
    if (group == null || !groups.containsKey(group)) {
      return null;
    }
    return groups.get(group).size();
  }

  public void sortSelectedItems() {
    sortItems(getSelectionModel().getSelectedIndices());
  }

  /**
   * Sorts items of the list alphabetically. Cases:
   *  - one item:
   *    * item is group header: sort group items
   *    * item is not group header: do nothing
   *  - multiple items:
   *    * sort group headers and not grouped elements together, sort grouped items within their groups
   *
   * Allows sorting of any possible list of list's indices.
   *
   * @param indices indices of items to sort
   */
  public void sortItems(List<Integer> indices) {
    if (indices == null || indices.isEmpty()) {
      return;
    }

    // Get items corresponding to indices
    List<GroupableListViewEntity> itemsToSort = indices.stream()
        .map(items::get)
        .collect(Collectors.toList());

    // One item is to be sorted
    if (indices.size() == 1) {

      // If item is group header, sort all group items, else do nothing
      if (itemsToSort.get(0) instanceof GroupEntity) {

        // Sort group items
        sortHomogeneousItems(getGroupItemsIndices((GroupEntity) itemsToSort.get(0)));
      }
      return;
    }

    // Split grouped and not grouped elements indices, distribute group elements by their groups
    List<Integer> notGroupedItemsIndices = new ArrayList<>();
    HashMap<GroupEntity, List<Integer>> groupedItemsIndices = new HashMap<>();
    for (Integer index : indices) {
      GroupableListViewEntity item = items.get(index);
      if (item instanceof ValueEntity && ((ValueEntity<?>) item).isGrouped()) {
        groupedItemsIndices.putIfAbsent(((ValueEntity<?>) item).getGroup(), new ArrayList<>());
        groupedItemsIndices.get(((ValueEntity<?>) item).getGroup()).add(index);
      } else {
        notGroupedItemsIndices.add(index);
      }
    }

    // Sort splitted items indices
    for (GroupEntity group : groupedItemsIndices.keySet()) {
      sortHomogeneousItems(groupedItemsIndices.get(group));
    }
    sortHomogeneousItems(notGroupedItemsIndices);

  }

  /**
   * Method designed to sort each part of splitted(e.g. : not grouped, belonging to group1,
   * belonging to group2...) in {@link GroupableListView#sortItems} items.
   * Optimized to sort only given indices, not affecting whole list.
   *
   * @param indices indices of items to sort
   */
  private void sortHomogeneousItems(List<Integer> indices) {
    if (indices == null || indices.size() < 2) {
      return;
    }

    // Get sorted items corresponding to indices
    List<GroupableListViewEntity> sortedItems = indices.stream()
        .map(items::get)
        .sorted(Ordering.usingToString())
        .collect(Collectors.toList());

    // Place sorted items one by one to the initial indices

    // List to save expanded groups to fill the list with their elements after sort
    List<GroupEntity> expandedGroups = new ArrayList<>();
    int shift = 0;
    for (int i = 0; i < indices.size(); i++) {
      indices.set(i, indices.get(i) - shift);
      GroupableListViewEntity item = items.get(indices.get(i));
      if (item instanceof GroupEntity && ((GroupEntity) item).isExpanded()) {
        shift += getGroupSize((GroupEntity) item);
        items.removeAll(groups.get(item));
        expandedGroups.add((GroupEntity) item);
      }
    }

    // Loop through initial indexes
    int sortedItemsIndex = 0;
    for (int index : indices) {

      GroupableListViewEntity item = sortedItems.get(sortedItemsIndex);

      // Put sorted element to the current index of the list view
      items.set(index, item);

      // If item is grouped, sort the group
      if (item instanceof ValueEntity && ((ValueEntity<T>) item).isGrouped()) {
        GroupEntity group = ((ValueEntity<?>) item).getGroup();
        groups.get(group).set(index - getGroupIndex(group) - 1, (ValueEntity<T>) item);
      }
      sortedItemsIndex++;
    }

    // Fill the list view with sorted groups elements
    for (GroupEntity expandedGroup : expandedGroups) {
      items.addAll(getGroupIndex(expandedGroup) + 1, getGroupItems(expandedGroup));
    }

    setItems(items);
  }

  /**
   * Returns {@link ValueEntity} of the list view, containing given value.
   *
   * @param value value
   * @return {@link ValueEntity} containing given value or null,
   * if such {@link ValueEntity} doesn't exist
   */
  @Nullable
  private ValueEntity<T> getValueEntity(T value) {
    for (GroupableListViewEntity item : items) {
      if (item instanceof ValueEntity && ((ValueEntity<?>) item).getValue().equals(value)) {
        return (ValueEntity<T>) item;
      }
    }
    return null;
  }

  /**
   * Generates new unique group name. Examples:
   * "Group" -> "Group", if "Group" doesn't exist
   * "Group" -> "Group(1)", if "Group" exists
   * "Group(1)" -> "Group(2)", if "Group(1)" exists
   *
   * @param groupName initial group name
   * @return new group name
   */
  private String generateNewGroupName(String groupName) {
    Set<String> groupsNames = groups.keySet().stream()
        .map(GroupEntity::getGroupName)
        .collect(Collectors.toSet());

    return generateNewGroupName(groupName, groupsNames, 1);
  }

  private String generateNewGroupName(String groupName, Set<String> groupsNames, int n) {
    if (!groupsNames.contains(groupName)) {
      return groupName;
    } else {
      return generateNewGroupName(n == 1
          ? groupName + "(" + n + ")"
          : groupName.substring(0, groupName.length() - 2) + n + ")", groupsNames, n + 1);
    }
  }

}
