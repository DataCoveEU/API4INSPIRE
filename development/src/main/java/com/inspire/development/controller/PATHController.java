package com.inspire.development.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PATHController implements ErrorController {

  @RequestMapping(value = "/error")
  public String handleError() {
    return "/";
  }

  @Override
  public String getErrorPath() {
    return "/error";
  }
}
