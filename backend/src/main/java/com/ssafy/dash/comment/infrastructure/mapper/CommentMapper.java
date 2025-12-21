package com.ssafy.dash.comment.infrastructure.mapper;

import com.ssafy.dash.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentMapper {

    void insert(Comment comment);

    Optional<Comment> selectById(Long id);

    List<Comment> selectByBoardId(Long boardId);

    List<Comment> selectByBoardIdAndLineNumber(@Param("boardId") Long boardId,
            @Param("lineNumber") Integer lineNumber);

    void update(Comment comment);

    void updateLikeCount(Comment comment);

    void delete(Long id);

    void deleteByBoardId(Long boardId);

    int countByBoardId(Long boardId);

}
