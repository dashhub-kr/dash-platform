package com.ssafy.dash.board.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.board.domain.Board;

@Mapper
public interface BoardMapper {

    void insert(Board board);
    
    Board selectById(Long id);
    
    List<Board> selectAll();
    
    void update(Board board);
    
    int delete(Long id);

}
