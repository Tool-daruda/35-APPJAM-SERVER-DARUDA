package com.daruda.darudaserver.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Plan;
import com.daruda.darudaserver.domain.tool.entity.RelatedTool;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolCore;
import com.daruda.darudaserver.domain.tool.entity.ToolImage;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;
import com.daruda.darudaserver.domain.tool.entity.ToolVideo;
import com.daruda.darudaserver.domain.tool.repository.PlanRepository;
import com.daruda.darudaserver.domain.tool.repository.RelatedToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolCoreRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolImageRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolKeywordRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolVideoRepository;
import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class AdminService {
	private final ToolRepository toolRepository;
	private final ToolVideoRepository toolVideoRepository;
	private final ToolKeywordRepository toolKeywordRepository;
	private final ToolCoreRepository toolCoreRepository;
	private final ToolImageRepository toolImageRepository;
	private final PlanRepository planRepository;
	private final RelatedToolRepository relatedToolRepository;

	@Transactional
	public void createTool(CreateToolRequest createToolRequest) {

		//Tool entity 가공
		Tool tool = Tool.builder()
			.toolLogo(createToolRequest.toolLogo())
			.toolLink(createToolRequest.toolLink())
			.toolMainName(createToolRequest.toolMainName())
			.toolSubName(createToolRequest.toolSubName())
			.bgColor(createToolRequest.bgColor())
			.category(Category.from(createToolRequest.category()))
			.description(createToolRequest.description())
			.detailDescription(createToolRequest.detailDescription())
			.license(License.from(createToolRequest.license()))
			.fontColor(createToolRequest.fontColor())
			.planLink(createToolRequest.planLink())
			.supportKorea(createToolRequest.supportKorea())
			.build();

		Tool savedTool = toolRepository.save(tool);

		//ToolVideo 가공
		List<ToolVideo> toolVideo = createToolRequest.videos().stream()
			.map(url -> ToolVideo.builder()
				.videoUrl(url)
				.tool(savedTool)
				.build())
			.toList();

		toolVideoRepository.saveAll(toolVideo);

		//ToolKeyword 가공
		List<String> keywords = createToolRequest.keywords();
		if (keywords != null && !keywords.isEmpty()) {
			List<ToolKeyword> keywordEntities = keywords.stream()
				.filter(keyword -> keyword != null && !keyword.isBlank())
				.map(keyword -> ToolKeyword.builder()
					.keywordName(keyword.trim())
					.tool(savedTool)
					.build())
				.toList();
			toolKeywordRepository.saveAll(keywordEntities);
		}

		//ToolImage 가공
		List<String> imageUrls = createToolRequest.images();
		if (imageUrls != null && !imageUrls.isEmpty()) {
			List<ToolImage> images = imageUrls.stream()
				.map(toolImage -> ToolImage.builder()
					.imageUrl(toolImage.trim())
					.tool(savedTool)
					.build())
				.toList();
			toolImageRepository.saveAll(images);
		}

		//ToolPlan 가공
		List<Plan> mapped = createToolRequest.plans().stream()
			.map(plan -> Plan.builder()
				.planName(plan.getPlanName().trim())
				.priceMonthly(plan.getPriceMonthly())
				.priceAnnual(plan.getPriceAnnual())
				.description(plan.getDescription())
				.isDollar(plan.getIsDollar())
				.tool(savedTool)
				.build())
			.toList();

		planRepository.saveAll(mapped);

		List<ToolCore> coreList = createToolRequest.cores().stream()
			.map(core -> {
				return ToolCore.builder()
					.coreTitle(core.getCoreTitle())
					.coreContent(core.getCoreContent())
					.tool(savedTool)
					.build();
			})
			.toList();
		toolCoreRepository.saveAll(coreList);

		// RelatedTool 가공
		List<Integer> relatedIds = createToolRequest.relatedToolIds();
		if (relatedIds != null && !relatedIds.isEmpty()) {
			List<Long> altIds = relatedIds.stream()
				.map(Integer::longValue)
				.toList();
			if (!altIds.isEmpty()) {
				List<Tool> alternatives = toolRepository.findAllById(altIds);
				if (!alternatives.isEmpty()) {
					List<RelatedTool> relations = alternatives.stream()
						.map(alt -> RelatedTool.builder()
							.alternativeTool(alt)
							.tool(savedTool)
							.build())
						.toList();
					if (!relations.isEmpty()) {
						relatedToolRepository.saveAll(relations);
					}
				}
			}
		}
	}
}
