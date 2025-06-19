package com.daruda.darudaserver.domain.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.daruda.darudaserver.domain.search.document.ToolDocument;

public interface ToolSearchRepository extends ElasticsearchRepository<ToolDocument, Long> {
}
