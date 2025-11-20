package com.ssafy.dash.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ssafy.dash.entity.RepoFile;

@Repository
public interface RepoFileRepository extends JpaRepository<RepoFile, Long> {

}
