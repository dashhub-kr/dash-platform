package com.ssafy.dash.study.infrastructure.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.dash.study.domain.Study;
import com.ssafy.dash.study.domain.StudyApplication;

@Mapper
public interface StudyMapper {

        void save(Study study);

        Optional<Study> findById(Long id);

        List<Study> findAll();

        List<Study> searchByKeyword(String keyword);

        void update(Study study);

        int delete(Long id);

        // Application Methods
        void saveApplication(com.ssafy.dash.study.domain.StudyApplication application);

        com.ssafy.dash.study.domain.StudyApplication findApplicationById(Long id);

        com.ssafy.dash.study.domain.StudyApplication findApplicationByStudyIdAndUserId(
                        @org.apache.ibatis.annotations.Param("studyId") Long studyId,
                        @org.apache.ibatis.annotations.Param("userId") Long userId);

        void updateApplicationStatus(@org.apache.ibatis.annotations.Param("id") Long id,
                        @org.apache.ibatis.annotations.Param("status") com.ssafy.dash.study.domain.StudyApplication.ApplicationStatus status);

        void deleteApplication(Long id);

        List<StudyApplication> findPendingApplicationsByStudyId(Long studyId);

        StudyApplication findPendingApplicationByUserId(Long userId);

}
