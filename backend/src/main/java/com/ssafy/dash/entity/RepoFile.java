package com.ssafy.dash.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "repo_file")
public class RepoFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repo_full_name", nullable = false)
    private String repoFullName;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public void setRepoFullName(String repoFullName) {
        this.repoFullName = repoFullName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(LocalDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

}
