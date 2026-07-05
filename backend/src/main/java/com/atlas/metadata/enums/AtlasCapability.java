package com.atlas.metadata.enums;

/** Product-level capabilities enforced by backend authorization. */
public enum AtlasCapability {
  AUTHENTICATED,
  SPACE_READ,
  SPACE_MANAGE,
  CONTENT_READ,
  CONTENT_WRITE,
  KNOWLEDGE_OPERATE,
  GOVERNANCE_READ,
  MEMBER_MANAGE,
  SETTINGS_MANAGE
}
