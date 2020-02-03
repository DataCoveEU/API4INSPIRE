package com.inspire.development.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ColumnConfig {
  private String alias;
  private boolean exclude;
  public ColumnConfig(@JsonProperty("alias") String alias,
      @JsonProperty("exclude") boolean exclude) {
    this.alias = alias;
    this.exclude = exclude;
  }

  public String getAlias() {
    return alias;
  }

  public boolean isExclude() {
    return exclude;
  }
}
