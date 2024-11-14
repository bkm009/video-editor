package com.bkm009.video_editor.utils;


import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class VideoEditorUtils {

    private static final ResourceLoader resourceLoader;

    private static final List<String> ALLOWED_VIDEO_FILE_EXTENSIONS = Arrays.asList("mp4");
    private static final long MAXIMUM_ALLOWED_VIDEO_SIZE_IN_BYTES = 25 * 1024 * 1024;   // 25 MB
    private static final short MINIMUM_ALLOWED_VIDEO_DURATION_IN_SECONDS = 5;
    private static final short MAXIMUM_ALLOWED_VIDEO_DURATION_IN_SECONDS = 25;

    static {
        resourceLoader = new DefaultResourceLoader();
    }

    // Validating Video File Extension
    public static boolean isValidVideoFile(String fileExtension) {
        return ALLOWED_VIDEO_FILE_EXTENSIONS.contains(fileExtension);
    }

    // Validating Video File Size
    public static boolean isValidVideoFileSize(long fileSize) {
        return MAXIMUM_ALLOWED_VIDEO_SIZE_IN_BYTES >= fileSize;
    }

    // Validating Video File Duration
    public static boolean isValidVideoDuration(double durationInSeconds) {
        return durationInSeconds >= MINIMUM_ALLOWED_VIDEO_DURATION_IN_SECONDS &&
                durationInSeconds <= MAXIMUM_ALLOWED_VIDEO_DURATION_IN_SECONDS;
    }

    public static double getVideoDuration(final File file) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                file.getAbsolutePath()
        );


        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String durationString = reader.readLine();
            if (durationString != null) {
                return Double.parseDouble(durationString);
            } else {
                throw new IOException("Could not retrieve video duration.");
            }
        }
    }

    // Get data Path
    public static String getDataDirectoryPath() {
        return Paths.get("").toAbsolutePath() + "/data/";
    }

    public static void trimVideo(String inputFilePath, String outputFilePath, double startSeconds, double durationSeconds) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-ss", String.valueOf(startSeconds), // Start time
                "-i", inputFilePath,                // Input file
                "-t", String.valueOf(durationSeconds), // Duration to keep
                "-c:v", "libx264",                  // Re-encode video with H.264 codec
                "-preset", "fast",                  // Use fast preset for speed
                "-c:a", "aac",                      // Re-encode audio with AAC codec
                "-strict", "experimental",          // Enable experimental features for AAC codec
                "-movflags", "+faststart",          // Enable fast start for video streaming
                outputFilePath
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Video trimming process was interrupted.", e);
        }

        // Check if the output file was created successfully
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new IOException("Failed to create trimmed video file.");
        }
    }

    public static void mergeVideo(String inputFilePaths, String outputFilePath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-f", "concat", "-safe", "0", "-i", inputFilePaths, "-c", "copy", outputFilePath
        );

        processBuilder.inheritIO();
        Process process = processBuilder.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Video merging process was interrupted.", e);
        }

        // Check if the output file was created successfully
        File outputFile = new File(outputFilePath);
        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new IOException("Failed to create merged video file.");
        }
    }

}
