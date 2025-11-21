package com.ssafy.dash.user;

import java.time.LocalDateTime;

public class User {

    private Long id;

    private Long githubId;

    private String username;

    private String avatarUrl;

    private String solvedAcHandle;

    private String accessToken;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getGithubId() {
		return githubId;
	}

	public void setGithubId(Long githubId) {
		this.githubId = githubId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl;
	}

	public String getSolvedAcHandle() {
		return solvedAcHandle;
	}

	public void setSolvedAcHandle(String solvedAcHandle) {
		this.solvedAcHandle = solvedAcHandle;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", githubId=" + githubId + ", username=" + username + ", avatarUrl=" + avatarUrl
				+ ", solvedAcHandle=" + solvedAcHandle + ", accessToken=" + accessToken + ", createdAt=" + createdAt
				+ ", updatedAt=" + updatedAt + "]";
	}

}
