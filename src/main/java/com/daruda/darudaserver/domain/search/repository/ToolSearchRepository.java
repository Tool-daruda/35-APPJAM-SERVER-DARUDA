package com.daruda.darudaserver.domain.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.search.document.ToolDocument;

@Repository
public interface ToolSearchRepository extends ElasticsearchRepository<ToolDocument, Long> {
}
