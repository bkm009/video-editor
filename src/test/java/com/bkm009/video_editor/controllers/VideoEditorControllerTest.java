package com.bkm009.video_editor.controllers;

import com.bkm009.video_editor.auth.ApiKeyValidator;
import com.bkm009.video_editor.configs.AuthConfigTest;
import com.bkm009.video_editor.models.VideoDto;
import com.bkm009.video_editor.services.VideoEditorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoEditorController.class)
@Import(AuthConfigTest.class)
class VideoEditorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoEditorService videoEditorService;

    @MockBean
    private ApiKeyValidator apiKeyValidator;

    private VideoDto mockVideoDto;

    @BeforeEach
    void setUp() {
        Mockito.when(apiKeyValidator.validateApiKey(anyString())).thenReturn(true);
        mockVideoDto = VideoDto.builder().videoId("1").fileName("sample.mp4").build();
    }

    @Test
    void testUploadVideo_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "sample.mp4", MediaType.APPLICATION_OCTET_STREAM_VALUE, "dummy data".getBytes());

        Mockito.when(videoEditorService.uploadVideo(any())).thenReturn(mockVideoDto);

        mockMvc.perform(multipart("/api/v1/video-editor/upload")
                        .file(file).header("x-api-key", "test_secret_api_key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.videoId").value("1"))
                .andExpect(jsonPath("$.data.fileName").value("sample.mp4"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetVideos_Success() throws Exception {
        Mockito.when(videoEditorService.getVideos()).thenReturn(Collections.singletonList(mockVideoDto));

        mockMvc.perform(get("/api/v1/video-editor/videos")
                        .header("x-api-key", "test_secret_api_key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].videoId").value("1"))
                .andExpect(jsonPath("$.data[0].fileName").value("sample.mp4"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testTrimVideo_Success() throws Exception {

        File file = new File("trimmed_sample.mp4");
        if(!file.exists()) {
            file.createNewFile();
        }

        Mockito.when(videoEditorService.trimVideo(anyString(), anyInt(), anyInt())).thenReturn(file);

        mockMvc.perform(get("/api/v1/video-editor/videos/1/trim")
                        .header("x-api-key", "test_secret_api_key")
                        .param("startTime", "5")
                        .param("endTime", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=trimmed_sample.mp4"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void testMergeVideo_Success() throws Exception {

        File file = new File("merged_sample.mp4");
        if(!file.exists()) {
            file.createNewFile();
        }

        Mockito.when(videoEditorService.mergeVideo(anyList())).thenReturn(file);

        mockMvc.perform(put("/api/v1/video-editor/videos/merge")
                        .header("x-api-key", "test_secret_api_key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"1\", \"2\"]"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=merged_sample.mp4"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    void testShareVideo_Success() throws Exception {
        Mockito.when(videoEditorService.shareVideo(anyString())).thenReturn("http://localhost:8080/shared/video/1");

        mockMvc.perform(get("/api/v1/video-editor/videos/1/share")
                        .header("x-api-key", "test_secret_api_key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("http://localhost:8080/shared/video/1"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
