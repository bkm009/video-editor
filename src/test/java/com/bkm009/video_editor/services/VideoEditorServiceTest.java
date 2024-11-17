package com.bkm009.video_editor.services;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.entities.VideoUploadEntity;
import com.bkm009.video_editor.models.VideoDto;
import com.bkm009.video_editor.repositories.VideoUploadRepository;
import com.bkm009.video_editor.utils.VideoEditorUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoEditorServiceTest {

    @Mock
    private VideoUploadRepository videoUploadRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LinkShareService linkShareService;

    @InjectMocks
    private VideoEditorService videoEditorService;

    @Mock
    private MultipartFile mockFile;

    private VideoUploadEntity videoUploadEntity;
    private VideoDto videoDto;

    @BeforeEach
    public void setUp() {
        // Set up mock videoUploadEntity and VideoDto for common test scenarios
        videoUploadEntity = new VideoUploadEntity();
        videoUploadEntity.setVideoId(UUID.randomUUID());
        videoUploadEntity.setFileName("test_video.mp4");
        videoUploadEntity.setVideoUrl("test_video.mp4");
        videoUploadEntity.setVideoDuration(15);

        videoDto = VideoDto.builder().videoId(videoUploadEntity.getVideoId().toString()).fileName(videoUploadEntity.getFileName()).build();
    }

    @Test
    public void testUploadVideo_DurationValid() throws IOException {
        // Arrange
        when(mockFile.getOriginalFilename()).thenReturn("test_video.mp4");
        when(mockFile.getBytes()).thenReturn("mock video data".getBytes());
        when(mockFile.isEmpty()).thenReturn(false);
        try (MockedStatic<VideoEditorUtils> utils = mockStatic(VideoEditorUtils.class)) {
            utils.when(() -> VideoEditorUtils.isValidVideoFile("mp4")).thenReturn(true);
            utils.when(() -> VideoEditorUtils.isValidVideoFileSize(anyLong())).thenReturn(true);
            utils.when(() -> VideoEditorUtils.getVideoDuration(any())).thenReturn(35.0);
            utils.when(() -> VideoEditorUtils.isValidVideoDuration(anyDouble())).thenReturn(false);
            utils.when(VideoEditorUtils::getDataDirectoryPath).thenReturn("");

            // Act
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                videoEditorService.uploadVideo(mockFile);
            });
            assertEquals("Video duration should be between 5 seconds to 25 seconds", exception.getMessage());
        }
    }

    @Test
    public void testUploadVideo_Success() throws IOException {
        // Arrange
        when(mockFile.getOriginalFilename()).thenReturn("test_video.mp4");
        when(mockFile.getBytes()).thenReturn("mock video data".getBytes());
        when(mockFile.isEmpty()).thenReturn(false);
        when(videoUploadRepository.save(any(VideoUploadEntity.class))).thenReturn(videoUploadEntity);
        try (MockedStatic<VideoEditorUtils> utils = mockStatic(VideoEditorUtils.class)) {
            utils.when(() -> VideoEditorUtils.isValidVideoFile("mp4")).thenReturn(true);
            utils.when(() -> VideoEditorUtils.isValidVideoFileSize(anyLong())).thenReturn(true);
            utils.when(() -> VideoEditorUtils.getVideoDuration(any())).thenReturn(15.0);
            utils.when(() -> VideoEditorUtils.isValidVideoDuration(anyDouble())).thenReturn(true);
            utils.when(VideoEditorUtils::getDataDirectoryPath).thenReturn("");

            String tempDirPath = VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.UPLOAD_VIDEO_DIRECTORY;
            File tempDir = new File(tempDirPath);
            if(!tempDir.exists())
                Files.createDirectory(Paths.get(tempDir.toURI()));

            // Act
            VideoDto result = videoEditorService.uploadVideo(mockFile);

            // Assert
            assertNotNull(result);
            assertEquals("test_video.mp4", result.getFileName());
            assertEquals(videoUploadEntity.getVideoId().toString(), result.getVideoId());
        }
    }

    @Test
    public void testUploadVideo_FileEmpty() throws IOException {
        // Arrange
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            videoEditorService.uploadVideo(mockFile);
        });
        assertEquals("Uploaded file does not contain any data", exception.getMessage());
    }

    @Test
    public void testUploadVideo_InvalidFormat() throws IOException {
        // Arrange
        when(mockFile.getOriginalFilename()).thenReturn("test_video.exe");
        when(mockFile.isEmpty()).thenReturn(false);

        try (MockedStatic<VideoEditorUtils> utils = mockStatic(VideoEditorUtils.class)) {
            utils.when(() -> VideoEditorUtils.isValidVideoFile("exe")).thenReturn(false);

            // Act & Assert
            BadRequestException exception = assertThrows(BadRequestException.class, () -> {
                videoEditorService.uploadVideo(mockFile);
            });
            assertEquals("Invalid Video format", exception.getMessage());
        }
    }

    @Test
    public void testGetVideos() {
        // Arrange
        when(videoUploadRepository.findAll()).thenReturn(List.of(videoUploadEntity));

        ArgumentCaptor<TypeReference<List<VideoDto>>> captor = ArgumentCaptor.forClass(TypeReference.class);
        when(objectMapper.convertValue(any(), captor.capture())).thenReturn(List.of(videoDto));

        // Act
        List<VideoDto> result = videoEditorService.getVideos();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(videoDto.getFileName(), result.get(0).getFileName());
    }

    @Test
    public void testTrimVideo_Success() throws IOException {
        // Arrange
        String videoId = videoUploadEntity.getVideoId().toString();
        int startTime = 5;
        int endTime = 10;
        try (MockedStatic<VideoEditorUtils> utils = mockStatic(VideoEditorUtils.class)) {
            utils.when(VideoEditorUtils::getDataDirectoryPath).thenReturn("");
            utils.when(() -> VideoEditorUtils.trimVideo(anyString(), anyString(), anyInt(), anyInt())).thenReturn(true);

            when(videoUploadRepository.findById(UUID.fromString(videoId))).thenReturn(Optional.of(videoUploadEntity));

            File file = new File("test_video.mp4");
            file.createNewFile();

            // Act
            File result = videoEditorService.trimVideo(videoId, startTime, endTime);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    public void testTrimVideo_InvalidTime() {
        // Arrange
        String videoId = videoUploadEntity.getVideoId().toString();
        int startTime = 10;
        int endTime = 5;

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            videoEditorService.trimVideo(videoId, startTime, endTime);
        });
        assertEquals("startTime & endTime is invalid", exception.getMessage());
    }

    @Test
    public void testMergeVideo_Success() throws IOException {
        // Arrange
        List<String> videoIds = List.of(videoUploadEntity.getVideoId().toString());
        try (MockedStatic<VideoEditorUtils> utils = mockStatic(VideoEditorUtils.class)) {
            utils.when(() -> VideoEditorUtils.getDataDirectoryPath()).thenReturn("");
            utils.when(() -> VideoEditorUtils.mergeVideo(anyString(), anyString())).thenReturn(true);

            when(videoUploadRepository.findAllById(any())).thenReturn(List.of(videoUploadEntity));

            String tempDirPath = VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.TEMP_DIRECTORY;
            File tempDir = new File(tempDirPath);
            if(!tempDir.exists())
                Files.createDirectory(Paths.get(tempDir.toURI()));

            File file = new File("test_video.mp4");
            file.createNewFile();

            // Act
            File result = videoEditorService.mergeVideo(videoIds);

            // Assert
            assertNotNull(result);
        }
    }

    @Test
    public void testShareVideo_Success() throws BadRequestException {
        // Arrange
        String videoId = videoUploadEntity.getVideoId().toString();
        String shareableLink = "http://example.com/share/" + videoUploadEntity.getVideoId();

        when(linkShareService.createShareableLink(any(VideoUploadEntity.class))).thenReturn(shareableLink);
        when(videoUploadRepository.findById(UUID.fromString(videoId))).thenReturn(Optional.of(videoUploadEntity));

        // Act
        String result = videoEditorService.shareVideo(videoId);

        // Assert
        assertNotNull(result);
        assertEquals(shareableLink, result);
    }

    @Test
    public void testShareVideo_InvalidVideoId() {
        // Arrange
        String invalidVideoId = UUID.randomUUID().toString();
        when(videoUploadRepository.findById(UUID.fromString(invalidVideoId))).thenReturn(Optional.empty());

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            videoEditorService.shareVideo(invalidVideoId);
        });
        assertEquals("Invalid videoId", exception.getMessage());
    }
}
