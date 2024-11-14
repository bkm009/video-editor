package com.bkm009.video_editor.controllers;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.models.GenericApiResponse;
import com.bkm009.video_editor.models.VideoDto;
import com.bkm009.video_editor.services.LinkShareService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.BASE_APP_URL + ApplicationConstants.SHARED_LINK_URL)
public class SharedLinkController {

    private static final Logger logger = LoggerFactory.getLogger(SharedLinkController.class);

    @Autowired
    private LinkShareService linkShareService;


    @GetMapping("/{linkId}")
    public ResponseEntity<Object> getSharedVideo(@PathVariable @NonNull String linkId) {

        File sharedVideoFile = null;

        try {

            sharedVideoFile = linkShareService.getSharedVideo(linkId);

            FileInputStream fileInputStream = new FileInputStream(sharedVideoFile);
            InputStreamResource resource = new InputStreamResource(fileInputStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + sharedVideoFile.getName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        }
        catch (BadRequestException badRequestException) {
            logger.error("[SharedLinkController.getSharedVideo] error : ", badRequestException);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error(badRequestException.getLocalizedMessage())
                    .success(false)
                    .statusCode(HttpServletResponse.SC_BAD_REQUEST)
                    .build());
        }
        catch (Exception exception) {
            logger.error("[SharedLinkController.getSharedVideo] error : ", exception);
            return ResponseEntity.internalServerError().body(GenericApiResponse.<List<VideoDto>>builder()
                    .error("Internal Server Error")
                    .success(false)
                    .statusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
                    .build());
        }
    }
}
