package com.daruda.darudaserver.domain.tool.controller;

import com.daruda.darudaserver.domain.tool.service.ToolService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tools")
public class ToolController {

    private final ToolService toolService;


}
