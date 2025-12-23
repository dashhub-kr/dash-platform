package com.ssafy.dash.problem.infrastructure.persistence;

import com.ssafy.dash.problem.domain.TagPrerequisite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TagPrerequisiteMapper {

    List<TagPrerequisite> findAll();

    List<TagPrerequisite> findByTagKey(String tagKey);

    List<TagPrerequisite> findByPrerequisiteTagKey(String prerequisiteTagKey);

    void insert(TagPrerequisite prerequisite);

    void insertBatch(List<TagPrerequisite> prerequisites);

    void deleteByTagKey(String tagKey);

    void deleteAll();
}
