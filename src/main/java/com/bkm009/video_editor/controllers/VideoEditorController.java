package com.bkm009.video_editor.controllers;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.models.GenericApiResponse;
import com.bkm009.video_editor.models.VideoDto;
import com.bkm009.video_editor.services.VideoEditorService;
import lombok.NonNull;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;


@RestController
@RequestMapping(ApplicationConstants.BASE_APP_URL + ApplicationConstants.VIDEO_EDITOR_URL)
public class VideoEditorController {

    Logger logger = LoggerFactory.getLogger(VideoEditorController.class);

    @Autowired
    private VideoEditorService videoEditorService;

    @PostMapping("/upload")
    public ResponseEntity<GenericApiResponse<VideoDto>> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {

            return ResponseEntity.ok(GenericApiResponse.<VideoDto>builder()
                    .data(videoEditorService.uploadVideo(file))
                    .success(true)
                    .statusCode(HttpServletResponse.SC_OK)
                    .build());
        }
        catch (BadRequestException badRequestException) {
            return ResponseEntity.badRequest().body(GenericApiResponse.<VideoDto>builder()
                    .error(badRequestException.getLocalizedMessage())
                    .success(false)
                    .statusCode(HttpServletResponse.SC_BAD_REQUEST)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[VideoEditorController.uploadVideo] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<VideoDto>builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    @GetMapping("/videos")
    public ResponseEntity<GenericApiResponse<List<VideoDto>>> getVideos() {
        try {

            return ResponseEntity.ok(GenericApiResponse.<List<VideoDto>>builder()
                    .data(videoEditorService.getVideos())
                    .success(true)
                    .statusCode(HttpServletResponse.SC_OK)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[VideoEditorController.getVideos] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
    }

    @GetMapping("/videos/{videoId}/trim")
    public ResponseEntity<Object> trimVideo(@PathVariable @NonNull String videoId,
            @RequestParam @NonNull int startTime, @RequestParam @NonNull int endTime) {

        File trimmedFile = null;

        try {

            trimmedFile = videoEditorService.trimVideo(videoId, startTime, endTime);

            FileInputStream fileInputStream = new FileInputStream(trimmedFile);
            InputStreamResource resource = new InputStreamResource(fileInputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + trimmedFile.getName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch (BadRequestException badRequestException) {
            logger.error("[VideoEditorController.trimVideo] error : ", badRequestException);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error(badRequestException.getLocalizedMessage())
                    .success(false)
                    .statusCode(HttpServletResponse.SC_BAD_REQUEST)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[VideoEditorController.trimVideo] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
        finally {
            if(trimmedFile != null && trimmedFile.exists()) {
                trimmedFile.delete();
            }
        }
    }


    @PutMapping("/videos/merge")
    public ResponseEntity<Object> mergeVideo(@RequestBody @NonNull List<String> videoIds) {

        File mergedFile = null;

        try {

            mergedFile = videoEditorService.mergeVideo(videoIds);

            FileInputStream fileInputStream = new FileInputStream(mergedFile);
            InputStreamResource resource = new InputStreamResource(fileInputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + mergedFile.getName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch (BadRequestException badRequestException) {
            logger.error("[VideoEditorController.mergeVideo] error : ", badRequestException);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error(badRequestException.getLocalizedMessage())
                    .success(false)
                    .statusCode(HttpServletResponse.SC_BAD_REQUEST)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[VideoEditorController.mergeVideo] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
        finally {
            if(mergedFile != null && mergedFile.exists()) {
                mergedFile.delete();
            }
        }
    }

    @GetMapping("/videos/{videoId}/share")
    public ResponseEntity<GenericApiResponse<Object>> shareVideo(@PathVariable @NonNull String videoId) {
        try{

            String sharedLink = videoEditorService.shareVideo(videoId);
            return ResponseEntity.ok().body(GenericApiResponse.builder()
                    .data(sharedLink)
                    .success(true)
                    .statusCode(HttpServletResponse.SC_OK)
                    .build());
        }
        catch (BadRequestException badRequestException) {
            logger.error("[VideoEditorController.shareVideo] error : ", badRequestException);
            return ResponseEntity.internalServerError().body(GenericApiResponse.builder()
                    .error(badRequestException.getLocalizedMessage())
                    .success(false)
                    .statusCode(HttpServletResponse.SC_BAD_REQUEST)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[VideoEditorController.shareVideo] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
    }
}
