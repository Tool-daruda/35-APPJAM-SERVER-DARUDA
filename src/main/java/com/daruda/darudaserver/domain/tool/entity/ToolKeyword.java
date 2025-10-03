package com.daruda.darudaserver.domain.tool.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Getter
@Builder
@Table(name = "tool_keyword")
public class ToolKeyword {

	@Id
	@Column(name = "keyword_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long keywordId;

	@Column(name = "keyword_name", nullable = false)
	private String keywordName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tool_id", nullable = false)
	private Tool tool;

}
