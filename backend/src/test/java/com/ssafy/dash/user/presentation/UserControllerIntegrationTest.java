package com.ssafy.dash.user.presentation;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dash.common.TestFixtures;
import com.ssafy.dash.user.presentation.dto.request.UserCreateRequest;
import com.ssafy.dash.user.presentation.dto.request.UserUpdateRequest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@DisplayName("UserController 통합 테스트")
@Disabled("테스트 환경 설정 필요 (DB, OAuth 등)")
@SuppressWarnings("null")
public class UserControllerIntegrationTest {

        @Autowired
        MockMvc mvc;

        @Autowired
        ObjectMapper mapper;

        @Test
        @DisplayName("유저 CRUD 전체 흐름을 수행하면 201-200-204 응답을 순차로 반환한다")
        public void crudFlow() throws Exception {
                // 유저 생성
                UserCreateRequest create = TestFixtures.createUserCreateRequest();
                String createJson = mapper.writeValueAsString(create);

                String location = mvc.perform(post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createJson))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").isNumber())
                                .andExpect(jsonPath("$.username").value(TestFixtures.TEST_USERNAME))
                                .andReturn()
                                .getResponse()
                                .getHeader("Location");

                if (location == null)
                        throw new IllegalStateException("Location header is missing");

                String[] parts = location.split("/");
                String id = parts[parts.length - 1];

                // 유저 조회
                mvc.perform(get("/api/users/" + id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value(TestFixtures.TEST_USERNAME));

                // 유저 수정
                UserUpdateRequest update = TestFixtures.createUserUpdateRequest();
                String updateJson = mapper.writeValueAsString(update);

                mvc.perform(put("/api/users/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.username").value(update.getUsername()));

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
