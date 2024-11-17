package com.bkm009.video_editor.controllers;

import com.bkm009.video_editor.auth.ApiKeyValidator;
import com.bkm009.video_editor.configs.AuthConfigTest;
import com.bkm009.video_editor.services.LinkShareService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SharedLinkController.class)
@Import(AuthConfigTest.class)
class SharedLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LinkShareService linkShareService;

    @MockBean
    private ApiKeyValidator apiKeyValidator;


    @Test
    void testGetSharedVideo_Success_Success() throws Exception {

        File file = new File("shared.mp4");
        if(!file.exists()) {
            file.createNewFile();
        }

        Mockito.when(linkShareService.getSharedVideo(anyString())).thenReturn(file);

        mockMvc.perform(get("/api/v1/shared/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=shared.mp4"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }
}
