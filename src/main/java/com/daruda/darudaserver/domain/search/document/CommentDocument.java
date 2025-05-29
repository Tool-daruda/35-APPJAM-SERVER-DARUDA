package com.daruda.darudaserver.domain.search.document;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Setting;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Setting(settingPath = "src/main/resources/elasticsearch/custom-settings.json")
@Document(indexName = "comment")
public class CommentDocument {

}
