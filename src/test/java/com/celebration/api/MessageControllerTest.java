package com.celebration.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetPublicMessage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("images", "cake.jpg", "image/jpeg", new byte[1024]);

        MvcResult createResult = mockMvc.perform(multipart("/api/v1/messages")
                        .file(image)
                        .param("title", "생일 축하해")
                        .param("content", "항상 고마워")
                        .param("occasionType", "BIRTHDAY")
                        .param("templateCode", "TEMPLATE_B"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.messageId").exists())
                .andReturn();

        MessageDtos.CreateMessageResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                MessageDtos.CreateMessageResponse.class
        );

        mockMvc.perform(get("/api/v1/messages/public/{publicToken}", created.publicToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", equalTo("생일 축하해")))
                .andExpect(jsonPath("$.templateCode", equalTo("TEMPLATE_B")))
                .andExpect(jsonPath("$.media.images[0].url").exists());
    }

    @Test
    void updateShouldFailWhenEditTokenInvalid() throws Exception {
        MvcResult createResult = mockMvc.perform(multipart("/api/v1/messages")
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("occasionType", "BIRTHDAY")
                        .param("templateCode", "TEMPLATE_A"))
                .andExpect(status().isCreated())
                .andReturn();

        MessageDtos.CreateMessageResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                MessageDtos.CreateMessageResponse.class
        );

        MessageDtos.UpdateMessageRequest body = new MessageDtos.UpdateMessageRequest(
                "새 제목",
                "새 내용",
                com.celebration.domain.TemplateCode.TEMPLATE_C,
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        mockMvc.perform(put("/api/v1/messages/{messageId}", created.messageId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Edit-Token", "wrong-token")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", equalTo("UNAUTHORIZED_EDIT_TOKEN")));
    }

    @Test
    void createShouldFailWhenVoiceExceedsLimit() throws Exception {
        MockMultipartFile voice = new MockMultipartFile("voice", "voice.mp3", "audio/mpeg", new byte[6 * 1024 * 1024]);

        mockMvc.perform(multipart("/api/v1/messages")
                        .file(voice)
                        .param("title", "제목")
                        .param("content", "내용")
                        .param("occasionType", "BIRTHDAY")
                        .param("templateCode", "TEMPLATE_A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", equalTo("VALIDATION_ERROR")));
    }
}
