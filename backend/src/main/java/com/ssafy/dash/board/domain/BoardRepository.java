package com.ssafy.dash.board.domain;

import java.util.List;
import java.util.Optional;

public interface BoardRepository {

    void save(Board board);

    Optional<Board> findById(Long id);

    Optional<Board> findByAlgorithmRecordId(Long recordId);

    List<Board> findAll();

    void update(Board board);

    void delete(Long id);

}
