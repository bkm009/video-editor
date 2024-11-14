package com.bkm009.video_editor.repositories;

import com.bkm009.video_editor.entities.VideoUploadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoUploadRepository extends JpaRepository<VideoUploadEntity, UUID> {
}
