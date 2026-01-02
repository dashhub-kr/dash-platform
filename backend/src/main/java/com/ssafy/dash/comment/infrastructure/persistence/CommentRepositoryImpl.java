package com.ssafy.dash.comment.infrastructure.persistence;

import com.ssafy.dash.comment.domain.Comment;
import com.ssafy.dash.comment.domain.CommentRepository;
import com.ssafy.dash.comment.infrastructure.mapper.CommentMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentMapper commentMapper;

    public CommentRepositoryImpl(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    @Override
    public void save(Comment comment) {
        commentMapper.insert(comment);
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    public List<Comment> findByBoardId(Long boardId, Long currentUserId) {
        return commentMapper.selectByBoardId(boardId, currentUserId);
    }

    @Override
    public List<Comment> findByBoardIdAndLineNumber(Long boardId, Integer lineNumber, Long currentUserId) {
        return commentMapper.selectByBoardIdAndLineNumber(boardId, lineNumber, currentUserId);
    }

    @Override
    public void update(Comment comment) {
        commentMapper.update(comment);
    }

    @Override
    public void updateLikeCount(Comment comment) {
        commentMapper.updateLikeCount(comment);
    }

    @Override
    public void delete(Long id) {
        commentMapper.delete(id);
    }

    @Override
    public void deleteByBoardId(Long boardId) {
        commentMapper.deleteByBoardId(boardId);
    }

    @Override
    public int countByBoardId(Long boardId) {
        return commentMapper.countByBoardId(boardId);
    }

}
