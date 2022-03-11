package com.hedera.hashgraph.identity.hcs.did;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.hedera.hashgraph.identity.DidDocumentBase;
import com.hedera.hashgraph.identity.DidSyntax;
import com.hedera.hashgraph.identity.DidSyntax.Method;
import com.hedera.hashgraph.identity.DidSyntax.MethodSpecificParameter;
import com.hedera.hashgraph.identity.HederaDid;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.bitcoinj.core.Base58;

/**
 * Hedera Decentralized Identifier for Hedera DID Method specification based on HCS.
 */
public class HcsDid implements HederaDid {
  public static final Method DID_METHOD = Method.HEDERA_HCS;
  private static final int DID_PARAMETER_VALUE_PARTS = 2;

  private TopicId didTopicId;
  private String network;
  private String idString;
  private String did;
  private PublicKey didRootKey;
  private PrivateKey privateDidRootKey;

  /**
   * Creates a DID instance.
   *
   * @param network           The Hedera DID network.
   * @param didRootKey        The public key from which DID is derived.
   * @param didTopicId        The appnet's DID topic ID.
   */
  public HcsDid(final String network, final PublicKey didRootKey,
                final TopicId didTopicId) {
    this.didTopicId = didTopicId;
    this.network = network;
    this.didRootKey = didRootKey;
    this.idString = HcsDid.publicKeyToIdString(didRootKey);
    this.did = buildDid();
  }

  /**
   * Creates a DID instance with private DID root key.
   *
   * @param network           The Hedera DID network.
   * @param privateDidRootKey The private DID root key.
   * @param didTopicId        The appnet's DID topic ID.
   */
  public HcsDid(final String network, final PrivateKey privateDidRootKey,
                final TopicId didTopicId) {
    this(network, privateDidRootKey.getPublicKey(), didTopicId);
    this.privateDidRootKey = privateDidRootKey;
  }

  /**
   * Creates a DID instance without topic ID specification.
   *
   * @param network           The Hedera DID network.
   * @param didRootKey        The public key from which DID is derived.
   */
  public HcsDid(final String network, final PublicKey didRootKey) {
    this(network, didRootKey, null);
  }

  /**
   * Creates a DID instance.
   *
   * @param network           The Hedera DID network.
   * @param idString          The id-string of a DID.
   * @param didTopicId        The appnet's DID topic ID.
   */
  public HcsDid(final String network, final String idString, final TopicId didTopicId) {
    this.didTopicId = didTopicId;
    this.network = network;

    this.idString = idString;
    this.did = buildDid();
  }

  /**
   * Converts a Hedera DID string into {@link HcsDid} object.
   *
   * @param didString A Hedera DID string.
   * @return {@link HcsDid} object derived from the given Hedera DID string.
   */
  public static HcsDid fromString(final String didString) {
    if (didString == null) {
      throw new IllegalArgumentException("DID string cannot be null");
    }

    // Split the DID string by parameter separator.
    // There should be at least one as address book parameter is mandatory by DID specification.
    Iterator<String> mainParts = Splitter.on(DidSyntax.DID_PARAMETER_SEPARATOR).split(didString).iterator();

    TopicId topicId = null;
    try {
      Iterator<String> didParts = Splitter.on(DidSyntax.DID_METHOD_SEPARATOR).split(mainParts.next()).iterator();

      if (!DidSyntax.DID_PREFIX.equals(didParts.next())) {
        throw new IllegalArgumentException("DID string is invalid: invalid prefix.");
      }

      String methodName = didParts.next();
      if (!Method.HEDERA_HCS.toString().equals(methodName)) {
        throw new IllegalArgumentException("DID string is invalid: invalid method name: " + methodName);
      }

      String networkName = didParts.next();
      // Extract method-specific parameters: address book file ID and (if provided) DID topic ID.
      Map<String, String> params = extractParameters(mainParts, methodName, networkName);
      if (params.containsKey(MethodSpecificParameter.DID_TOPIC_ID)) {
        topicId = TopicId.fromString(params.get(MethodSpecificParameter.DID_TOPIC_ID));
      }

      String didIdString = didParts.next();
      if (didIdString.length() < 32 || didParts.hasNext()) {
        throw new IllegalArgumentException("DID string is invalid.");
      }

      return new HcsDid(networkName, didIdString, topicId);
    } catch (NoSuchElementException e) {
      throw new IllegalArgumentException("DID string is invalid.", e);
    }
  }

  /**
   * Extracts method-specific URL parameters.
   *
   * @param mainParts   Iterator over main parts of the DID.
   * @param methodName  The method name.
   * @param networkName The network name.
   * @return A map of method-specific URL parameters and their values.
   */
  private static Map<String, String> extractParameters(final Iterator<String> mainParts,
                                                       final String methodName, final String networkName) {

    Map<String, String> result = new HashMap<>();

    String fidParamName = String.join(DidSyntax.DID_METHOD_SEPARATOR, methodName, networkName);
    String tidParamName = String.join(DidSyntax.DID_METHOD_SEPARATOR, methodName, networkName,
            MethodSpecificParameter.DID_TOPIC_ID);

    while (mainParts.hasNext()) {
      String[] paramValue = mainParts.next().split(DidSyntax.DID_PARAMETER_VALUE_SEPARATOR);
      if (paramValue.length != DID_PARAMETER_VALUE_PARTS) {
        continue;
      } else if (tidParamName.equals(paramValue[0])) {
        result.put(MethodSpecificParameter.DID_TOPIC_ID, paramValue[1]);
      }
    }

//    // Address book is mandatory
//    if (!result.containsKey(MethodSpecificParameter.ADDRESS_BOOK_FILE_ID)) {
//      throw new IllegalArgumentException("DID string is invalid. Required method-specific URL parameter not found: "
//              + MethodSpecificParameter.ADDRESS_BOOK_FILE_ID);
//    }

    return result;
  }

  /**
   * Generates a random DID root key.
   *
   * @return A private key of generated public DID root key.
   */
  public static PrivateKey generateDidRootKey() {
    return PrivateKey.generate();
  }

  /**
   * Constructs an id-string of a DID from a given public key.
   *
   * @param didRootKey Public Key from which the DID is created.
   * @return The id-string of a DID that is a Base58-encoded SHA-256 hash of a given public key.
   */
  public static String publicKeyToIdString(final PublicKey didRootKey) {
    return Base58.encode(Hashing.sha256().hashBytes(didRootKey.toBytes()).asBytes());
  }

  @Override
  public String toDid() {
    return toString();
  }

  @Override
  public DidDocumentBase generateDidDocument() {
    DidDocumentBase result = new DidDocumentBase(this.toDid());
    if (didRootKey != null) {
      HcsDidRootKey rootKey = HcsDidRootKey.fromHcsIdentity(this, didRootKey);
      result.setDidRootKey(rootKey);
    }

    return result;
  }

  /**
   * Generates DID document base from the given DID and its root key.
   *
   * @param didRootKey Public key used to build this DID.
   * @return The DID document base.
   * @throws IllegalArgumentException In case given DID root key does not match this DID.
   */
  public DidDocumentBase generateDidDocument(final PublicKey didRootKey) {
    DidDocumentBase result = new DidDocumentBase(this.toDid());

    if (didRootKey != null) {
      HcsDidRootKey rootKey = HcsDidRootKey.fromHcsIdentity(this, didRootKey);
      result.setDidRootKey(rootKey);
    }

    return result;
  }

  @Override
  public String getNetwork() {
    return network;
  }

  @Override
  public Method getMethod() {
    return Method.HEDERA_HCS;
  }

  @Override
  public String toString() {
    return did;
  }

  public TopicId getDidTopicId() {
    return didTopicId;
  }

  public String getIdString() {
    return idString;
  }

  /**
   * Constructs DID string from the instance of DID object.
   *
   * @return A DID string.
   */
  private String buildDid() {
    String methodNetwork = String.join(DidSyntax.DID_METHOD_SEPARATOR, getMethod().toString(), network);

    StringBuilder sb = new StringBuilder()
            .append(DidSyntax.DID_PREFIX)
            .append(DidSyntax.DID_METHOD_SEPARATOR)
            .append(methodNetwork)
            .append(DidSyntax.DID_METHOD_SEPARATOR)
            .append(idString)
            .append(DidSyntax.DID_PARAMETER_SEPARATOR)
            .append(methodNetwork)
            .append(DidSyntax.DID_METHOD_SEPARATOR)
            .append(DidSyntax.DID_PARAMETER_VALUE_SEPARATOR);

    if (didTopicId != null) {
      sb.append(DidSyntax.DID_PARAMETER_SEPARATOR)
              .append(methodNetwork)
              .append(DidSyntax.DID_METHOD_SEPARATOR)
              .append(MethodSpecificParameter.DID_TOPIC_ID)
              .append(DidSyntax.DID_PARAMETER_VALUE_SEPARATOR)
              .append(didTopicId);
    }

    return sb.toString();
  }

  /**
   * Returns a private key of DID root key.
   * This is only available if it was provided during {@link HcsDid} construction.
   *
   * @return The private key of DID root key.
   */
  public Optional<PrivateKey> getPrivateDidRootKey() {
    return Optional.ofNullable(privateDidRootKey);
  }
}
