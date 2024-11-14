package com.bkm009.video_editor.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name = "link_share")
public class LinkShareEntity {

    @Id
    private UUID linkId;

    @ManyToOne(targetEntity = VideoUploadEntity.class)
    private VideoUploadEntity videoUploadEntity;

    private long createdAt;
}
