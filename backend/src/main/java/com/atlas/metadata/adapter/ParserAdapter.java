package com.atlas.metadata.adapter;

/** Product-facing parser adapter contract. */
public interface ParserAdapter {

  /** Returns masked capability metadata for this adapter. */
  ParserCapability capability();

  /** Parses PDF metadata into Markdown/assets/source chunk results. */
  ParserResult parse(ParserRequest request);
}
