package com.adaptris.downloader.resolvers.ivy;

import org.apache.ivy.util.Message;

enum IvyLogLevel {

  ERROR(Message.MSG_ERR),
  WARN(Message.MSG_WARN),
  INFO(Message.MSG_INFO),
  VERBOSE(Message.MSG_VERBOSE),
  DEBUG(Message.MSG_DEBUG);

  private final int level;

  IvyLogLevel(int level) {
    this.level = level;
  }

  public int getLevel() {
    return level;
  }

  /**
   * Return the IvyLogLevel for the given string value ignoring case. If the value is none of the enum values it return the
   * {@link IvyLogLevel#ERROR}
   *
   * @param value
   * @return enum matching the given string
   */
  public static IvyLogLevel valueOrError(String value) {
    IvyLogLevel[] values = values();
    for (IvyLogLevel ivyLogLevel : values) {
      if (ivyLogLevel.toString().equalsIgnoreCase(value)) {
        return ivyLogLevel;
      }
    }
    return ERROR;
  }

}
