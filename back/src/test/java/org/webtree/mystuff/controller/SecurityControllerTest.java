package org.webtree.mystuff.controller;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MvcResult;
import org.webtree.mystuff.domain.User;
import org.webtree.mystuff.security.JwtTokenUtil;
import org.webtree.mystuff.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecurityControllerTest extends BaseControllerTest {
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASS = "testPass";
    @Rule
    public ClearGraphDBRule clearGraphDBRule = new ClearGraphDBRule();

    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenUtil tokenUtil;

    @Test
    public void whenLoginWithCorrectUser_shouldReturnValidToken() throws Exception {
        User user = User.builder().username(TEST_USERNAME).password(TEST_PASS).build();
        userService.add(user);

        MvcResult mvcResult = mockMvc.perform(
            post("/rest/token/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user))
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").doesNotExist())
            .andReturn();
        String token = mvcResult.getResponse().getContentAsString();
        assertThat(tokenUtil.validateToken(token, user)).isTrue();
    }

    @Test
    @WithAnonymousUser
    public void whenRefreshWithInvalidToken_shouldReturnError() throws Exception {
        mockMvc.perform(get("/rest/token/refresh"))
            .andExpect(status().isUnauthorized());
    }
}