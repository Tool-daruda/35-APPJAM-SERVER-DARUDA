package com.daruda.darudaserver.domain.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.daruda.darudaserver.domain.search.document.BoardDocument;

@Repository
public interface BoardSearchRepository extends ElasticsearchRepository<BoardDocument, Long> {

}
