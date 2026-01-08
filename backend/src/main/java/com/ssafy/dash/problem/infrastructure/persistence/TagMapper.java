package com.ssafy.dash.problem.infrastructure.persistence;

import com.ssafy.dash.problem.domain.Tag;
import com.ssafy.dash.problem.domain.TagFamily;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TagMapper {
    void saveTagFamily(TagFamily tagFamily);

    void saveTag(Tag tag);

    TagFamily findFamilyByKey(String familyKey);

    List<TagFamily> findAllFamilies();

    Tag findTagByKey(String key);

    List<Tag> findAllTags();

    List<Tag> findCoreTagsByFamilyId(@Param("familyId") Long familyId);

    List<Tag> findCandidateTags();

    void updateTagMetadata(Tag tag);
}
