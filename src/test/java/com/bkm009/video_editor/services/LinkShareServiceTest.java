package com.bkm009.video_editor.services;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.entities.LinkShareEntity;
import com.bkm009.video_editor.entities.VideoUploadEntity;
import com.bkm009.video_editor.repositories.LinkShareRepository;
import com.bkm009.video_editor.utils.VideoEditorUtils;
import lombok.SneakyThrows;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LinkShareServiceTest {

    @Mock
    private LinkShareRepository linkShareRepository;

    @InjectMocks
    private LinkShareService linkShareService;

    private VideoUploadEntity videoUploadEntity;
    private LinkShareEntity linkShareEntity;

    @BeforeEach
    public void setUp() {
        // Initialize videoUploadEntity with mock values
        videoUploadEntity = new VideoUploadEntity();
        videoUploadEntity.setVideoUrl("sample_video.mp4");

        // Initialize LinkShareEntity with mock values
        linkShareEntity = new LinkShareEntity();
        linkShareEntity.setLinkId(UUID.randomUUID());
        linkShareEntity.setCreatedAt(System.currentTimeMillis());
        linkShareEntity.setVideoUploadEntity(videoUploadEntity);
    }

    @Test
    public void testCreateShareableLink() {
        // Arrange
        when(linkShareRepository.save(any(LinkShareEntity.class))).thenReturn(linkShareEntity);

        // Act
        String shareableLink = linkShareService.createShareableLink(videoUploadEntity);

        // Assert
        assertNotNull(shareableLink);
        assertTrue(shareableLink.contains(ApplicationConstants.BASE_APP_URL + ApplicationConstants.SHARED_LINK_URL));
        assertTrue(shareableLink.contains(linkShareEntity.getLinkId().toString()));

        verify(linkShareRepository, times(1)).save(any(LinkShareEntity.class));
    }

    @SneakyThrows
    @Test
    public void testGetSharedVideo_Success() throws BadRequestException {
        // Arrange
        String linkId = linkShareEntity.getLinkId().toString();
        String mockFilePath = videoUploadEntity.getVideoUrl();

        try (MockedStatic<VideoEditorUtils> mockedStatic = mockStatic(VideoEditorUtils.class)) {
            mockedStatic.when(VideoEditorUtils::getDataDirectoryPath).thenReturn("");  // Mock static method

            when(linkShareRepository.findById(UUID.fromString(linkId))).thenReturn(Optional.of(linkShareEntity));

            File mockedFile = new File(mockFilePath);
            if(!mockedFile.exists()) mockedFile.createNewFile();

            // Act
            File result = linkShareService.getSharedVideo(linkId);

            // Assert
            assertNotNull(result);
            assertEquals(mockedFile, result);
            verify(linkShareRepository, times(1)).findById(UUID.fromString(linkId));
        }
    }

    @Test
    public void testGetSharedVideo_InvalidLink() {
        // Arrange
        String invalidLinkId = UUID.randomUUID().toString();
        when(linkShareRepository.findById(UUID.fromString(invalidLinkId))).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            linkShareService.getSharedVideo(invalidLinkId);
        });

        assertEquals("Invalid link", exception.getMessage());
        verify(linkShareRepository, times(1)).findById(UUID.fromString(invalidLinkId));
    }

    @Test
    public void testGetSharedVideo_LinkExpired() {
        // Arrange
        String linkId = linkShareEntity.getLinkId().toString();
        long expiredTime = System.currentTimeMillis() - (ApplicationConstants.LINK_EXPIRY_IN_MILLIS + 1);
        linkShareEntity.setCreatedAt(expiredTime);
        when(linkShareRepository.findById(UUID.fromString(linkId))).thenReturn(Optional.of(linkShareEntity));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            linkShareService.getSharedVideo(linkId);
        });

        assertEquals("Link expired", exception.getMessage());
    }

    @Test
    public void testGetSharedVideo_VideoNotFound() {
        // Arrange
        String linkId = linkShareEntity.getLinkId().toString();
        when(linkShareRepository.findById(UUID.fromString(linkId))).thenReturn(Optional.of(linkShareEntity));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            linkShareService.getSharedVideo(linkId);
        });

        assertEquals("Video resource not found", exception.getMessage());
    }
}
