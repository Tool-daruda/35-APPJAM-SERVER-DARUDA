package com.daruda.darudaserver.domain.tool.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;

/**
 * QTool is a Querydsl query type for Tool
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QTool extends EntityPathBase<Tool> {

	private static final long serialVersionUID = -208495251L;

	public static final QTool tool = new QTool("tool");

	public final StringPath bgColor = createString("bgColor");

	public final EnumPath<Category> category = createEnum("category", Category.class);

	public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

	public final StringPath description = createString("description");

	public final StringPath detailDescription = createString("detailDescription");

	public final BooleanPath fontColor = createBoolean("fontColor");

	public final EnumPath<License> license = createEnum("license", License.class);

	public final StringPath planLink = createString("planLink");

	public final NumberPath<Integer> popular = createNumber("popular", Integer.class);

	public final BooleanPath supportKorea = createBoolean("supportKorea");

	public final NumberPath<Long> toolId = createNumber("toolId", Long.class);

	public final StringPath toolLink = createString("toolLink");

	public final StringPath toolLogo = createString("toolLogo");

	public final StringPath toolMainName = createString("toolMainName");

	public final StringPath toolSubName = createString("toolSubName");

	public final DateTimePath<java.sql.Timestamp> updatedAt = createDateTime("updatedAt", java.sql.Timestamp.class);

	public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

	public QTool(String variable) {
		super(Tool.class, forVariable(variable));
	}

	public QTool(Path<? extends Tool> path) {
		super(path.getType(), path.getMetadata());
	}

	public QTool(PathMetadata metadata) {
		super(Tool.class, metadata);
	}

}

