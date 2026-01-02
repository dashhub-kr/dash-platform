package com.ssafy.dash.comment.domain;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    void save(Comment comment);

    Optional<Comment> findById(Long id);

    List<Comment> findByBoardId(Long boardId, Long currentUserId);

    List<Comment> findByBoardIdAndLineNumber(Long boardId, Integer lineNumber, Long currentUserId);

    void update(Comment comment);

    void updateLikeCount(Comment comment);

    void delete(Long id);

    void deleteByBoardId(Long boardId);

    int countByBoardId(Long boardId);

}
