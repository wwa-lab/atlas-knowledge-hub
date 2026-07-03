package com.atlas.metadata.adapter;

import com.atlas.metadata.enums.StorageAdapterStatus;
import com.atlas.metadata.enums.StorageLayer;
import com.atlas.metadata.enums.StorageObjectStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** S3-compatible storage boundary that reports masked capability until real runtime wiring exists. */
@Component
public class ConfiguredS3CompatibleStorageAdapter implements StorageAdapter {

  public static final String ADAPTER_KEY = "s3-compatible";
  private static final String SAFE_UNAVAILABLE = "Configured storage runtime is not enabled for this slice.";

  @Override
  public StorageCapability capability() {
    return new StorageCapability(
        ADAPTER_KEY,
        "S3-Compatible Object Storage",
        "configured",
        List.of(StorageLayer.values()),
        false,
        StorageAdapterStatus.MISCONFIGURED,
        Map.of(
            "endpoint", "missing",
            "bucket", "missing",
            "region", "missing",
            "credentials", "missing",
            "externalNetwork", "disabled"));
  }

  @Override
  public StorageObjectDescriptor put(StoragePutRequest request) {
    return unavailable(request.layer(), request.objectKey(), request.contentType(), request.fileId());
  }

  @Override
  public StorageObjectDescriptor get(StorageObjectRef ref) {
    return unavailable(ref.layer(), ref.objectKey(), null, ref.fileId());
  }

  @Override
  public boolean exists(StorageObjectRef ref) {
    return false;
  }

  @Override
  public StorageListResult list(StorageListRequest request) {
    return new StorageListResult(List.of(), null);
  }

  @Override
  public StorageDeleteResult delete(StorageObjectRef ref) {
    return new StorageDeleteResult(unavailable(ref.layer(), ref.objectKey(), null, ref.fileId()));
  }

  private StorageObjectDescriptor unavailable(
      StorageLayer layer, String objectKey, String contentType, String fileId) {
    return new StorageObjectDescriptor(
        layer,
        objectKey,
        contentType,
        null,
        null,
        ADAPTER_KEY,
        StorageObjectStatus.FAILED,
        fileId,
        SAFE_UNAVAILABLE);
  }
}
