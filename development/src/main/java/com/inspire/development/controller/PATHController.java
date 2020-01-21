package com.inspire.development.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PATHController implements ErrorController {

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    public String handleError() {
        return "/";
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}
