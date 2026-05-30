package com.yby.api.repository;

import com.yby.api.entity.SyncJob;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncJobRepository extends JpaRepository<SyncJob, UUID> {
}
