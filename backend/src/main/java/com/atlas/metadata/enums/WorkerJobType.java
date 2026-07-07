package com.atlas.metadata.enums;

/** Local worker job categories supported by the retry/dead-letter foundation. */
public enum WorkerJobType {
  BATCH_INGEST,
  CONNECTOR_SYNC,
  ASYNC_PROCESSING
}
