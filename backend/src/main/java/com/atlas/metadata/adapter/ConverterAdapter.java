package com.atlas.metadata.adapter;

/** Product-facing converter adapter contract. */
public interface ConverterAdapter {

  /** Returns masked capability metadata for this adapter. */
  ConverterCapability capability();

  /** Converts source file metadata into PDF conversion results. */
  ConverterResult convert(ConverterRequest request);
}
