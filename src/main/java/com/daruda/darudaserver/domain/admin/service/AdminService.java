package com.daruda.darudaserver.domain.admin.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.daruda.darudaserver.domain.admin.dto.request.CreateToolCoreRequest;
import com.daruda.darudaserver.domain.admin.dto.request.CreateToolPlanRequest;
import com.daruda.darudaserver.domain.admin.dto.request.CreateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.request.UpdateToolRequest;
import com.daruda.darudaserver.domain.admin.dto.response.AdminToolPageRes;
import com.daruda.darudaserver.domain.community.repository.BoardRepository;
import com.daruda.darudaserver.domain.tool.entity.Category;
import com.daruda.darudaserver.domain.tool.entity.License;
import com.daruda.darudaserver.domain.tool.entity.Plan;
import com.daruda.darudaserver.domain.tool.entity.PlanType;
import com.daruda.darudaserver.domain.tool.entity.RelatedTool;
import com.daruda.darudaserver.domain.tool.entity.Tool;
import com.daruda.darudaserver.domain.tool.entity.ToolBlog;
import com.daruda.darudaserver.domain.tool.entity.ToolCore;
import com.daruda.darudaserver.domain.tool.entity.ToolImage;
import com.daruda.darudaserver.domain.tool.entity.ToolKeyword;
import com.daruda.darudaserver.domain.tool.entity.ToolPlatForm;
import com.daruda.darudaserver.domain.tool.entity.ToolVideo;
import com.daruda.darudaserver.domain.tool.repository.PlanRepository;
import com.daruda.darudaserver.domain.tool.repository.RelatedToolRepository;
import com.daruda.darudaserver.domain.tool.repository.ToolBlogRepository;
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
	private final ToolBlogRepository toolBlogRepository;

	public void createTool(CreateToolRequest createToolRequest) {

		//Tool entity 가공
		Tool tool = Tool.from(createToolRequest);

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
		List<CreateToolPlanRequest> planRequests = createToolRequest.plans();
		if (planRequests != null && !planRequests.isEmpty()) {
			List<Plan> planEntities = planRequests.stream()
				.filter(Objects::nonNull)
				.map(planRequest -> Plan.create(planRequest, savedTool))
				.toList();
			if (!planEntities.isEmpty()) {
				planRepository.saveAll(planEntities);
			}
		}

		//ToolBlog 가공
		List<String> blogLinks = createToolRequest.blogLinks();
		if (blogLinks != null && !blogLinks.isEmpty()) {
			List<ToolBlog> blogEntities = blogLinks.stream()
				.filter(link -> link != null && !link.isBlank())
				.map(link -> ToolBlog.create(link.trim(), savedTool))
				.toList();
			if (!blogEntities.isEmpty()) {
				toolBlogRepository.saveAll(blogEntities);
			}
		}

		//ToolCore 가공
		List<CreateToolCoreRequest> coreList = createToolRequest.cores();
		if (coreList != null && !coreList.isEmpty()) {
			List<ToolCore> coreEntities = coreList.stream()
				.filter(Objects::nonNull)
				.map(core -> ToolCore.create(core, savedTool))
				.toList();
			if (!coreEntities.isEmpty()) {
				toolCoreRepository.saveAll(coreEntities);
			}
		}

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

		// ToolPlatForm 가공
		if (createToolRequest.toolPlatForm() != null) {
			ToolPlatForm toolPlatForm = ToolPlatForm.of(createToolRequest.toolPlatForm(), tool);
			toolPlatFormRepository.save(toolPlatForm);
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
			req.toolLogo() != null ? req.toolLogo() : tool.getToolLogo(),
			req.planType() != null ? PlanType.formString(req.planType()) : tool.getPlanType()
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
					.filter(c -> c != null && c.coreName() != null && c.coreContent() != null)
					.map(c -> ToolCore.builder()
						.coreTitle(c.coreName().trim())
						.coreContent(c.coreContent())
						.tool(tool)
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
					.filter(p -> p != null && p.planName() != null && p.planPrice() != null)
					.map(p -> Plan.builder()
						.planName(p.planName().trim())
						.price(p.planPrice())
						.description(p.planDescription())
						.tool(tool)
						.build())
					.toList();
				if (!toSave.isEmpty()) {
					planRepository.saveAll(toSave);
				}
			}
		}

		// 툴 블로그 수정
		if (req.blogLinks() != null) {
			List<ToolBlog> existing = toolBlogRepository.findAllByTool(tool);
			if (!existing.isEmpty()) {
				toolBlogRepository.deleteAll(existing);
			}
			if (!req.blogLinks().isEmpty()) {
				List<ToolBlog> toSave = req.blogLinks().stream()
					.filter(link -> link != null && !link.isBlank())
					.map(link -> ToolBlog.create(link.trim(), tool))
					.toList();
				if (!toSave.isEmpty()) {
					toolBlogRepository.saveAll(toSave);
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
				List<Long> altIds = req.relatedToolIds();
				List<Tool> alternatives = toolRepository.findAllById(altIds);
				if (!alternatives.isEmpty()) {
					List<RelatedTool> relations = alternatives.stream()
						.map(alt -> RelatedTool.builder().alternativeTool(alt).tool(tool).build())
						.toList();
					relatedToolRepository.saveAll(relations);
				}
			}
		}

		// 툴 플랫폼 수정
		if (req.toolPlatForm() != null) {
			toolPlatFormRepository.findFirstByTool(tool).ifPresent(toolPlatFormRepository::delete);

			ToolPlatForm newPlatForm = ToolPlatForm.of(req.toolPlatForm(), tool);
			toolPlatFormRepository.save(newPlatForm);
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
		toolBlogRepository.deleteByTool(tool);

		// 2) 연관 툴(양방향 FK) 모두 제거
		relatedToolRepository.deleteByTool(tool);
		relatedToolRepository.deleteByAlternativeTool(tool);

		// 3) 커뮤니티 글에서 FK 해제
		boardRepository.clearTool(tool);

		// 4) 마지막에 툴 삭제
		toolRepository.delete(tool);
	}

	public AdminToolPageRes fetchAllTool(String criteria, String direction, int page, int size) {
		Sort.Direction dir = Sort.Direction.fromString(direction);
		Pageable pageable = PageRequest.of(page, size, Sort.by(dir, criteria));
		Page<Tool> toolPage = toolRepository.findAll(pageable);

		return AdminToolPageRes.of(toolPage);
	}
}
