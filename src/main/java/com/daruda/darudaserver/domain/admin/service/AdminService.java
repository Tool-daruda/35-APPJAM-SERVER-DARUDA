package com.daruda.darudaserver.domain.admin.service;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.request.UpdateToolRequest;
import com.daruda.darudaserver.domain.tool.entity.*;
import com.daruda.darudaserver.domain.tool.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
	private final ToolPlatFormRepository toolPlatFormRepository;


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
					.coreTitle(core.coreTitle())
					.coreContent(core.coreContent())
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

	@Transactional
	public void updateTool(final Long toolId, final UpdateToolRequest req) {
		Tool tool = toolRepository.findById(toolId)
			.orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolId));

		// 툴 정보 수정
		tool.update(
			req.toolMainName() != null ? req.toolMainName() : tool.getToolMainName(),
			req.toolSubName() != null ? req.toolSubName() : tool.getToolSubName(),
			req.category() != null ? Category.from(req.category()) : tool.getCategory(),
			req.toolLink() != null ? req.toolLink() : tool.getToolLink(),
			req.description() != null ? req.description() : tool.getDescription(),
			req.license() != null ? License.from(req.license()) : tool.getLicense(),
			req.supportKorea() != null ? req.supportKorea() : tool.getSupportKorea(),
			req.detailDescription() != null ? req.detailDescription() : tool.getDetailDescription(),
			req.planLink() != null ? req.planLink() : tool.getPlanLink(),
			req.bgColor() != null ? req.bgColor() : tool.getBgColor(),
			req.fontColor() != null ? req.fontColor() : tool.isFontColor(),
			req.toolLogo() != null ? req.toolLogo() : tool.getToolLogo()
		);

		// 툴 키워드 수정
		if (req.keywords() != null) {
			List<ToolKeyword> existing = toolKeywordRepository.findAllByTool(tool);
			if (!existing.isEmpty()) toolKeywordRepository.deleteAll(existing);
			if (!req.keywords().isEmpty()) {
				List<ToolKeyword> toSave = req.keywords().stream()
					.filter(k -> k != null && !k.isBlank())
					.map(k -> ToolKeyword.builder().keywordName(k.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) toolKeywordRepository.saveAll(toSave);
			}
		}

		// 툴 이미지 수정
		if (req.images() != null) {
			List<ToolImage> existing = toolImageRepository.findAllByTool(tool);
			if (!existing.isEmpty()) toolImageRepository.deleteAll(existing);
			if (!req.images().isEmpty()) {
				List<ToolImage> toSave = req.images().stream()
					.filter(url -> url != null && !url.isBlank())
					.map(url -> ToolImage.builder().imageUrl(url.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) toolImageRepository.saveAll(toSave);
			}
		}

		// 툴 비디오 수정
		if (req.videos() != null) {
			List<ToolVideo> existing = toolVideoRepository.findAllByTool(tool);
			if (!existing.isEmpty()) toolVideoRepository.deleteAll(existing);
			if (!req.videos().isEmpty()) {
				List<ToolVideo> toSave = req.videos().stream()
					.filter(url -> url != null && !url.isBlank())
					.map(url -> ToolVideo.builder().videoUrl(url.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) toolVideoRepository.saveAll(toSave);
			}
		}

		// 툴 코어 수정
		if (req.cores() != null) {
			List<ToolCore> existing = toolCoreRepository.findAllByTool(tool);
			if (!existing.isEmpty()) toolCoreRepository.deleteAll(existing);
			if (!req.cores().isEmpty()) {
				List<ToolCore> toSave = req.cores().stream()
					.filter(c -> c != null && c.getCoreTitle() != null && c.getCoreContent() != null)
					.map(c -> ToolCore.builder().coreTitle(c.getCoreTitle()).coreContent(c.getCoreContent()).tool(tool)
						.build())
					.toList();
				if (!toSave.isEmpty()) toolCoreRepository.saveAll(toSave);
			}
		}

		// 툴 플랜 수정
		if (req.plans() != null) {
			List<Plan> existing = planRepository.findAllByTool(tool);
			if (!existing.isEmpty()) planRepository.deleteAll(existing);
			if (!req.plans().isEmpty()) {
				List<Plan> toSave = req.plans().stream()
					.filter(p -> p != null && p.getPlanName() != null && p.getPriceMonthly() != null)
					.map(p -> Plan.builder()
						.planName(p.getPlanName().trim())
						.priceMonthly(p.getPriceMonthly())
						.priceAnnual(p.getPriceAnnual())
						.description(p.getDescription())
						.isDollar(p.getIsDollar())
						.tool(tool)
						.build())
					.toList();
				if (!toSave.isEmpty()) planRepository.saveAll(toSave);
			}
		}

		// 툴 관련 툴 수정
		if (req.relatedToolIds() != null) {
			List<RelatedTool> existing = relatedToolRepository.findAllByTool(tool);
			if (!existing.isEmpty()) relatedToolRepository.deleteAll(existing);
			if (!req.relatedToolIds().isEmpty()) {
				List<Long> altIds = req.relatedToolIds().stream().map(Integer::longValue).toList();
				List<Tool> alternatives = toolRepository.findAllById(altIds);
				if (!alternatives.isEmpty()) {
					List<RelatedTool> relations = alternatives.stream()
						.map(alt -> RelatedTool.builder().alternativeTool(alt).tool(tool).build())
						.toList();
					relatedToolRepository.saveAll(relations);
				}
			}
		}

		// 갱신된 엔터티 저장
		toolRepository.save(tool);
	}
}
