package com.ssafy.dash.board.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.dash.board.domain.Board;
import com.ssafy.dash.board.domain.BoardRepository;
import com.ssafy.dash.board.infrastructure.mapper.BoardMapper;

@Repository
public class BoardRepositoryImpl implements BoardRepository {

    private final BoardMapper boardMapper;

    public BoardRepositoryImpl(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    @Override
    public void save(Board board) {
        boardMapper.insert(board);
    }

    @Override
    public Optional<Board> findById(Long id) {
        return Optional.ofNullable(boardMapper.selectById(id));
    }

    @Override
    public Optional<Board> findByAlgorithmRecordId(Long recordId) {
        return Optional.ofNullable(boardMapper.selectByAlgorithmRecordId(recordId));
    }

    @Override
    public List<Board> findAll() {
        return boardMapper.selectAll();
    }

    @Override
    public void update(Board board) {
        boardMapper.update(board);
    }

    @Override
    public void delete(Long id) {
        boardMapper.delete(id);
    }

}
