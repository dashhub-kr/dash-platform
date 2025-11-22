package com.ssafy.dash.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ssafy.dash.user.dto.UserCreateRequest;
import com.ssafy.dash.user.dto.UserUpdateRequest;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper mapper;

    @Test
    public void crudFlow() throws Exception {
        // 유저 생성
        UserCreateRequest create = new UserCreateRequest("alice", "alice@example.com");
        String createJson = mapper.writeValueAsString(create);

        String location = mvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.username").value("alice"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

        String[] parts = location.split("/");
        String id = parts[parts.length - 1];

        // 유저 조회
        mvc.perform(get("/api/users/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice"));

        // 유저 수정
        UserUpdateRequest update = new UserUpdateRequest("alice2", "alice2@example.com");
        String updateJson = mapper.writeValueAsString(update);

        mvc.perform(put("/api/users/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("alice2"));

        // 유저 목록
        mvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").isNumber());

        // 유저 삭제
        mvc.perform(delete("/api/users/" + id))
            .andExpect(status().isNoContent());

        // 유저 삭제 후 찾을 수 없음
        mvc.perform(get("/api/users/" + id))
            .andExpect(status().isNotFound());
    }

}
