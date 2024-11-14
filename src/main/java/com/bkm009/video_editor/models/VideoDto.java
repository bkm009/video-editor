package com.bkm009.video_editor.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VideoDto {

    private String videoId;
    private String fileName;
}
