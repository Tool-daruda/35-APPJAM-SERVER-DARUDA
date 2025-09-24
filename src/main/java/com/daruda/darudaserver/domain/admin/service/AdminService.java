package com.daruda.darudaserver.domain.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {
	private final ToolRepository toolRepository;

	@Transactional
	public void createTool(CreateToolRequest createToolRequest) {

	}
}
