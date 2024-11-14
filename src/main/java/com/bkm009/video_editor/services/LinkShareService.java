package com.bkm009.video_editor.services;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.entities.LinkShareEntity;
import com.bkm009.video_editor.entities.VideoUploadEntity;
import com.bkm009.video_editor.repositories.LinkShareRepository;
import com.bkm009.video_editor.utils.VideoEditorUtils;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

@Service
public class LinkShareService {

    @Autowired
    private LinkShareRepository linkShareRepository;

    @Value("${spring.server.port}")
    private String serverPort;

    public String createShareableLink(VideoUploadEntity videoUploadEntity) {

        LinkShareEntity linkShareEntity = new LinkShareEntity();
        linkShareEntity.setLinkId(UUID.randomUUID());
        linkShareEntity.setCreatedAt(System.currentTimeMillis());
        linkShareEntity.setVideoUploadEntity(videoUploadEntity);

        linkShareRepository.save(linkShareEntity);

        return ApplicationConstants.BASE_APP_URL + ApplicationConstants.SHARED_LINK_URL +
                "/" + linkShareEntity.getLinkId();
    }

    public File getSharedVideo(String linkId) throws BadRequestException {

        Optional<LinkShareEntity> linkShareEntity = linkShareRepository.findById(UUID.fromString(linkId));
        if(linkShareEntity.isEmpty()) {
            throw new BadRequestException("Invalid link");
        }

        long expiryTime = linkShareEntity.get().getCreatedAt() + ApplicationConstants.LINK_EXPIRY_IN_MILLIS;
        if(System.currentTimeMillis() > expiryTime) {
            throw new BadRequestException("Link expired");
        }

        String inputFilePath = VideoEditorUtils.getDataDirectoryPath() + linkShareEntity.get().getVideoUploadEntity().getVideoUrl();
        File videoFile = new File(inputFilePath);
        if(!videoFile.exists()) {
            throw new BadRequestException("Video resource not found");
        }

        return videoFile;
    }
}
