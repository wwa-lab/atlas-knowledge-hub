package com.atlas.metadata.repository;

import com.atlas.metadata.domain.FileItem;
import com.atlas.metadata.enums.FileStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for file item metadata. */
public interface FileItemRepository extends JpaRepository<FileItem, String> {

  /** Finds all file items for metric derivation. */
  List<FileItem> findByBatchId(String batchId);

  /** Finds file items for multiple batches. */
  List<FileItem> findByBatchIdIn(List<String> batchIds);

  /** Finds selected file items within one batch. */
  List<FileItem> findByBatchIdAndIdIn(String batchId, List<String> ids);

  /** Finds paged file items by batch. */
  Page<FileItem> findByBatchId(String batchId, Pageable pageable);

  /** Finds paged file items by batch and status. */
  Page<FileItem> findByBatchIdAndStatus(String batchId, FileStatus status, Pageable pageable);
}
