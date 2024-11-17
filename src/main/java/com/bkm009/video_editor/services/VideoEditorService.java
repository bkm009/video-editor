package com.bkm009.video_editor.services;

import com.bkm009.video_editor.constants.ApplicationConstants;
import com.bkm009.video_editor.entities.VideoUploadEntity;
import com.bkm009.video_editor.models.VideoDto;
import com.bkm009.video_editor.repositories.VideoUploadRepository;
import com.bkm009.video_editor.utils.VideoEditorUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VideoEditorService {


    @Autowired
    private VideoUploadRepository videoUploadRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LinkShareService linkShareService;


    public VideoEditorService() throws IOException {

        File resourceDataDirectory = new File(VideoEditorUtils.getDataDirectoryPath());
        if(!resourceDataDirectory.exists())
            Files.createDirectory(Path.of(resourceDataDirectory.toURI()));

        // Creating temp directory if not exists
        File tempDataDirectory = new File(VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.TEMP_DIRECTORY);
        if(!tempDataDirectory.exists())
            Files.createDirectory(Paths.get(tempDataDirectory.toURI()));

        // Creating uploads directory if not exists
        File uploadsDataDirectory = new File(VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.UPLOAD_VIDEO_DIRECTORY);
        if(!uploadsDataDirectory.exists())
            Files.createDirectory(Paths.get(uploadsDataDirectory.toURI()));
    }


    public VideoDto uploadVideo(final MultipartFile file) throws IOException {

        File tempFile = null;

        try {
            if (file.isEmpty()) {
                throw new BadRequestException("Uploaded file does not contain any data");
            }

            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileExtension = StringUtils.getFilenameExtension(fileName);
            if (!VideoEditorUtils.isValidVideoFile(fileExtension)) {
                throw new BadRequestException("Invalid Video format");
            }

            // Validating video size
            if(!VideoEditorUtils.isValidVideoFileSize(file.getSize())) {
                throw new BadRequestException("Video size allowed upto 25 MB");
            }

            String videoStorageDir = VideoEditorUtils.getDataDirectoryPath();

            // Temporary save to check video duration
            UUID videoId = UUID.randomUUID();
            String tempFileName = videoId + "_" + fileName;
            Path tempFilePath = Paths.get(videoStorageDir, ApplicationConstants.TEMP_DIRECTORY + tempFileName);
            Files.write(tempFilePath, file.getBytes());

            // Validating video duration
            tempFile = new File(tempFilePath.toString());
            double videoDurationInSeconds = VideoEditorUtils.getVideoDuration(tempFile);
            if (!VideoEditorUtils.isValidVideoDuration(videoDurationInSeconds)) {
                throw new BadRequestException("Video duration should be between 5 seconds to 25 seconds");
            }

            String videoPath = ApplicationConstants.UPLOAD_VIDEO_DIRECTORY + tempFileName;
            Files.move(tempFilePath, Path.of(videoStorageDir, videoPath));

            VideoUploadEntity videoUploadEntity = new VideoUploadEntity();
            videoUploadEntity.setVideoId(videoId);
            videoUploadEntity.setVideoDuration(videoDurationInSeconds);
            videoUploadEntity.setVideoUrl(videoPath);
            videoUploadEntity.setFileName(fileName);

            videoUploadEntity = videoUploadRepository.save(videoUploadEntity);
            return VideoDto.builder().videoId(videoUploadEntity.getVideoId().toString())
                    .fileName(videoUploadEntity.getFileName()).build();

        }
        finally {
            if(Objects.nonNull(tempFile) && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public List<VideoDto> getVideos() {

        List<VideoUploadEntity> videos = videoUploadRepository.findAll();
        List<VideoDto> videoDtos = objectMapper.convertValue(videos, new TypeReference<List<VideoDto>>(){});
        return videoDtos;
    }

    public File trimVideo(String videoId, int startTime, int endTime) throws IOException {

        if(startTime==endTime || startTime > endTime || startTime <0) {
            throw new BadRequestException("startTime & endTime is invalid");
        }

        Optional<VideoUploadEntity> videoUploadEntity = videoUploadRepository.findById(UUID.fromString(videoId));
        if(videoUploadEntity.isEmpty()) {
            throw new BadRequestException("Invalid videoId");
        }

        if(endTime > videoUploadEntity.get().getVideoDuration()) {
            throw new BadRequestException("Invalid endTime");
        }

        String inputFilePath = VideoEditorUtils.getDataDirectoryPath() + videoUploadEntity.get().getVideoUrl();
        File inputFile = new File(inputFilePath);
        if(!inputFile.exists()) {
            throw new BadRequestException("Video resource not found");
        }

        String outputFilePath = VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.TEMP_DIRECTORY +
                "trimmed_" + System.currentTimeMillis() + "_" + videoUploadEntity.get().getFileName();

        VideoEditorUtils.trimVideo(inputFilePath, outputFilePath, startTime, (endTime - startTime));
        return new File(outputFilePath);
    }


    public File mergeVideo(List<String> videoIds) throws IOException {

        List<UUID> videosUuids = videoIds.stream().map(UUID::fromString).collect(Collectors.toList());
        List<VideoUploadEntity> videos = videoUploadRepository.findAllById(videosUuids);

        if(videos.size() != videoIds.size()) {
            throw new BadRequestException("Invalid videoId present");
        }

        UUID id = UUID.randomUUID();
        String inputFilePaths = VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.TEMP_DIRECTORY + id + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(inputFilePaths))) {
            for(VideoUploadEntity video : videos) {
                String inputFilePath = VideoEditorUtils.getDataDirectoryPath() + video.getVideoUrl();
                File inputFile = new File(inputFilePath);
                if(!inputFile.exists()) {
                    throw new BadRequestException("Video resource not found");
                }

                writer.write("file '" + inputFilePath + "'");
                writer.newLine();
            }
        }

        String outputFilePath = VideoEditorUtils.getDataDirectoryPath() + ApplicationConstants.TEMP_DIRECTORY +
                "merged_" + id + ".mp4";

        VideoEditorUtils.mergeVideo(inputFilePaths, outputFilePath);

        File listFile = new File(inputFilePaths);
        if(listFile.exists()) {
            listFile.delete();
        }

        return new File(outputFilePath);
    }

    public String shareVideo(String videoId) throws BadRequestException {

        Optional<VideoUploadEntity> videoUploadEntity = videoUploadRepository.findById(UUID.fromString(videoId));
        if(videoUploadEntity.isEmpty()) {
            throw new BadRequestException("Invalid videoId");
        }

        return linkShareService.createShareableLink(videoUploadEntity.get());
    }
}
