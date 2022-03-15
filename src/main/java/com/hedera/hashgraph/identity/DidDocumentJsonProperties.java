package com.hedera.hashgraph.identity;

/**
 * Key property names in DID document standard.
 */
public final class DidDocumentJsonProperties {
  public static final String CONTEXT = "@context";
  public static final String ID = "id";
  public static final String CONTROLLER = "controller";
  public static final String AUTHENTICATION = "authentication";
  public static final String VERIFICATION_METHOD = "verificationMethod";
  public static final String ASSERTION_METHOD = "assertionMethod";
  public static final String KEY_AGREEMENT = "keyAgreement";
  public static final String CAPABILITY_INVOCATION = "capabilityInvocation";
  public static final String CAPABILITY_DELEGATION = "capabilityDelegation";
  public static final String SERVICE = "service";

  /**
   * This class is not to be instantiated.
   */
  private DidDocumentJsonProperties() {
    // Empty on purpose.
  }
}
