/*
 * Copyright (C) 2018 Seoul National University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.snu.mist.core.task.groupaware;

/**
 * This is an event for group addition/deletion.
 */
public final class GroupEvent {

  /**
   * Group event type: addition or deletion.
   */
  public enum GroupEventType {
    ADDITION,
    DELETION
  }

  /**
   * Group info.
   */
  private final Group groupInfo;

  /**
   * Group event type.
   */
  private final GroupEventType type;

  public GroupEvent(final Group groupInfo,
                    final GroupEventType type) {
    this.groupInfo = groupInfo;
    this.type = type;
  }

  /**
   * Get the type of the group event.
   * @return group event type
   */
  public GroupEventType getGroupEventType() {
    return type;
  }

  /**
   * Get the group info.
   * @return group info
   */
  public Group getGroupInfo() {
    return groupInfo;
  }
}