package com.atlas.metadata.adapter.runtime;

/** Adapter-internal runtime executor boundary. */
@FunctionalInterface
public interface RuntimeExecutor {

  /** Executes one bounded runtime invocation. */
  RuntimeExecutionResult execute(RuntimeInvocation invocation);
}
