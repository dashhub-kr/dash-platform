package com.ssafy.dash.problem.infrastructure.persistence;

import com.ssafy.dash.problem.domain.Problem;
import com.ssafy.dash.problem.domain.ProblemTag;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProblemMapper {
    void saveProblem(Problem problem);

    void saveProblemTag(ProblemTag problemTag);

    void deleteProblemTags(String problemNumber);

    Problem findProblemByNumber(String problemNumber);

    int countProblems();

    List<String> findTagsByProblemNumber(String problemNumber);

    List<Problem> findProblemsByTagAndLevelRange(@org.apache.ibatis.annotations.Param("tagKey") String tagKey,
            @org.apache.ibatis.annotations.Param("minLevel") int minLevel,
            @org.apache.ibatis.annotations.Param("maxLevel") int maxLevel,
            @org.apache.ibatis.annotations.Param("excludedIds") List<String> excludedIds);

    List<Problem> findProblemsByNumbers(
            @org.apache.ibatis.annotations.Param("problemNumbers") List<String> problemNumbers);

    /**
     * 여러 문제의 태그를 한 번에 조회 (N+1 쿼리 방지)
     * @param problemNumbers 문제 번호 목록
     * @return 문제번호-태그 쌍 목록
     */
    List<ProblemTag> findTagsByProblemNumbers(
            @org.apache.ibatis.annotations.Param("problemNumbers") List<String> problemNumbers);
}
