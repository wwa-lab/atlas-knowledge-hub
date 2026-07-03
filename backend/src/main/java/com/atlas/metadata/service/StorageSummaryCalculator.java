package com.atlas.metadata.service;

import com.atlas.metadata.domain.StorageObject;
import com.atlas.metadata.domain.StorageOperation.StorageSummary;
import com.atlas.metadata.enums.StorageObjectStatus;
import java.util.List;

/** Derives storage operation summary counts from persisted object descriptors. */
public class StorageSummaryCalculator {

  /** Computes status counts and stored bytes from storage object rows. */
  public StorageSummary compute(List<StorageObject> objects, int skipped) {
    int stored = count(objects, StorageObjectStatus.STORED);
    int deleted = count(objects, StorageObjectStatus.DELETED);
    int missing = count(objects, StorageObjectStatus.MISSING);
    int failed = count(objects, StorageObjectStatus.FAILED);
    long totalBytes =
        objects.stream()
            .filter(object -> object.getStatus() == StorageObjectStatus.STORED)
            .map(StorageObject::getSizeBytes)
            .filter(size -> size != null)
            .mapToLong(Long::longValue)
            .sum();
    return new StorageSummary(
        objects.size() + skipped, stored, deleted, missing, failed, skipped, totalBytes);
  }

  private int count(List<StorageObject> objects, StorageObjectStatus status) {
    return (int) objects.stream().filter(object -> object.getStatus() == status).count();
  }
}
