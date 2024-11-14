package com.bkm009.video_editor.repositories;

import com.bkm009.video_editor.entities.LinkShareEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkShareRepository extends JpaRepository<LinkShareEntity, UUID> {
}
