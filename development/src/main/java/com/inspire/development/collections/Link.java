package com.inspire.development.collections;

public class Link {
  String href;
  String rel;
  String type;
  String title;
  public Link(String href, String rel, String type, String title) {
    this.href = href;
    this.rel = rel;
    this.type = type;
    this.title = title;
  }

  public String getHref() {
    return href;
  }

  public String getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getRel() {
    return rel;
  }
}
