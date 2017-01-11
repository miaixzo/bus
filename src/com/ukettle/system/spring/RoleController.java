package com.ukettle.system.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ukettle.engine.loader.BasicController;

@Controller
@RequestMapping(BasicController.VIEW_SYSTEM + BasicController.VIEW_ROLE)
public class RoleController extends BasicController {

}