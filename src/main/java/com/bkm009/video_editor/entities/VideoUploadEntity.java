package com.bkm009.video_editor.entities;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name = "video_uploads")
public class VideoUploadEntity {

    @Id
    private UUID videoId;

    private String fileName;
    private String videoUrl;
    private double videoDuration;
}
