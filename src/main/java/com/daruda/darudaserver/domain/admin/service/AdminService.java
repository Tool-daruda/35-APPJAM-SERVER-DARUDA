package com.daruda.darudaserver.domain.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.request.UpdateToolRequest;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
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
import com.daruda.darudaserver.domain.tool.repository.ToolPlatFormRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolScrapRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolVideoRepository;
import com.daruda.darudaserver.global.error.code.ErrorCode;
import com.daruda.darudaserver.global.error.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Transactional
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
	private final BoardRepository boardRepository;
	private final ToolScrapRepository toolScrapRepository;

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
			.map(core -> ToolCore.builder()
				.coreTitle(core.getCoreTitle())
				.coreContent(core.getCoreContent())
				.tool(savedTool)
				.build())
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
			if (!existing.isEmpty()) {
				toolKeywordRepository.deleteAll(existing);
			}
			if (!req.keywords().isEmpty()) {
				List<ToolKeyword> toSave = req.keywords().stream()
					.filter(k -> k != null && !k.isBlank())
					.map(k -> ToolKeyword.builder().keywordName(k.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) {
					toolKeywordRepository.saveAll(toSave);
				}
			}
		}

		// 툴 이미지 수정
		if (req.images() != null) {
			List<ToolImage> existing = toolImageRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				toolImageRepository.deleteAll(existing);
			}
			if (!req.images().isEmpty()) {
				List<ToolImage> toSave = req.images().stream()
					.filter(url -> url != null && !url.isBlank())
					.map(url -> ToolImage.builder().imageUrl(url.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) {
					toolImageRepository.saveAll(toSave);
				}
			}
		}

		// 툴 비디오 수정
		if (req.videos() != null) {
			List<ToolVideo> existing = toolVideoRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				toolVideoRepository.deleteAll(existing);
			}
			if (!req.videos().isEmpty()) {
				List<ToolVideo> toSave = req.videos().stream()
					.filter(url -> url != null && !url.isBlank())
					.map(url -> ToolVideo.builder().videoUrl(url.trim()).tool(tool).build())
					.toList();
				if (!toSave.isEmpty()) {
					toolVideoRepository.saveAll(toSave);
				}
			}
		}

		// 툴 코어 수정
		if (req.cores() != null) {
			List<ToolCore> existing = toolCoreRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				toolCoreRepository.deleteAll(existing);
			}
			if (!req.cores().isEmpty()) {
				List<ToolCore> toSave = req.cores().stream()
					.filter(c -> c != null && c.getCoreTitle() != null && c.getCoreContent() != null)
					.map(c -> ToolCore.builder().coreTitle(c.getCoreTitle()).coreContent(c.getCoreContent()).tool(tool)
						.build())
					.toList();
				if (!toSave.isEmpty()) {
					toolCoreRepository.saveAll(toSave);
				}
			}
		}

		// 툴 플랜 수정
		if (req.plans() != null) {
			List<Plan> existing = planRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				planRepository.deleteAll(existing);
			}
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
				if (!toSave.isEmpty()) {
					planRepository.saveAll(toSave);
				}
			}
		}

		// 툴 관련 툴 수정
		if (req.relatedToolIds() != null) {
			List<RelatedTool> existing = relatedToolRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				relatedToolRepository.deleteAll(existing);
			}
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

	public void deleteTool(final Long toolId) {
		Tool tool = toolRepository.findById(toolId)
			.orElseThrow(() -> new NotFoundException(ErrorCode.TOOL_NOT_FOUND));

		// 관련된 엔터티들 삭제 (자식 → 부모 순서)
		// 1) 연관 테이블들 선삭제
		toolKeywordRepository.deleteByTool(tool);
		toolImageRepository.deleteByTool(tool);
		toolVideoRepository.deleteByTool(tool);
		toolCoreRepository.deleteByTool(tool);
		planRepository.deleteByTool(tool);
		toolPlatFormRepository.deleteByTool(tool);
		toolScrapRepository.deleteByTool(tool);

		// 2) 연관 툴(양방향 FK) 모두 제거
		relatedToolRepository.deleteByTool(tool);
		relatedToolRepository.deleteByAlternativeTool(tool);

		// 3) 커뮤니티 글에서 FK 해제
		boardRepository.clearTool(tool);

		// 4) 마지막에 툴 삭제
		toolRepository.delete(tool);
	}
}
