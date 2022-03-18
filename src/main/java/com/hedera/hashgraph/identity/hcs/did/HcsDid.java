package com.hedera.hashgraph.identity.hcs.did;

import com.google.common.base.Splitter;
import com.google.common.hash.Hashing;
import com.hedera.hashgraph.identity.*;
import com.hedera.hashgraph.identity.DidSyntax.Method;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TopicId;
import org.bitcoinj.core.Base58;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Hedera Decentralized Identifier for Hedera DID Method specification based on HCS.
 */
public class HcsDid implements HederaDid {
  public static final Method DID_METHOD = Method.HEDERA_HCS;

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
    Iterator<String> mainParts = Splitter.on(DidSyntax.DID_TOPIC_SEPARATOR).split(didString).iterator();

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
      String didIdString = didParts.next();
      if (didIdString.length() < 32){
        throw new IllegalArgumentException("DID string is invalid.");
      }
      if (mainParts.hasNext()) {
        topicId = TopicId.fromString(mainParts.next());
      }

      return new HcsDid(networkName, didIdString, topicId);
    } catch (NoSuchElementException e) {
      throw new IllegalArgumentException("DID string is invalid.", e);
    }
  }


  /**
   * Generates a random DID root key.
   *
   * @return A private key of generated public DID root key.
   */
  public static PrivateKey generateDidRootKey() {
    return PrivateKey.generateED25519() ;
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
            .append(DidSyntax.DID_TOPIC_SEPARATOR);

    if (didTopicId != null) {
      sb.append(didTopicId);
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

  public static void parseIdentifier(String identifier) throws DidError {
    String[] array = identifier.split(DidSyntax.DID_TOPIC_SEPARATOR);
    if (array.length == 2) {
      String topicIdPart = array[1];
      if (topicIdPart.isEmpty()) {
        throw new DidError("DID string is invalid: topic ID is missing", DidErrorCode.INVALID_DID_STRING);
      }

      TopicId topicId = TopicId.fromString(topicIdPart);

      String[] didParts = array[0].split(DidSyntax.DID_METHOD_SEPARATOR);
      if (didParts.length == 4) {
        if (!Objects.equals(didParts[0], DidSyntax.DID_PREFIX)) {
          throw new DidError("DID string is invalid: invalid prefix.", DidErrorCode.INVALID_DID_STRING);
        }

        String methodName = didParts[1];
        if (!Objects.equals(Method.HEDERA_HCS.toString(), methodName)) {
          throw new DidError(
                  "DID string is invalid: invalid method name: " + methodName,
                  DidErrorCode.INVALID_DID_STRING
          );
        }

        try {
            String networkName = didParts[2];

          if (
                  !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_MAINNET) &&
                          !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_TESTNET) &&
                          !Objects.equals(networkName, DidSyntax.HEDERA_NETWORK_PREVIEWNET)
          ) {
            throw new DidError("DID string is invalid. Invalid Hedera network.", DidErrorCode.INVALID_NETWORK);
          }

          String didIdString = didParts[3];

          if (didIdString.length() < 48) {
            throw new DidError(
                    "DID string is invalid. ID holds incorrect format.",
                    DidErrorCode.INVALID_DID_STRING
            );
          }

         // return [networkName, topicId, didIdString];

        } catch (Exception e) {
          if (e instanceof DidError) {
            throw e;
          }

          throw new DidError("DID string is invalid. " + e.getMessage(), DidErrorCode.INVALID_DID_STRING);
        }
      } else{
        throw new DidError(
                "DID string is invalid. ID holds incorrect format.",
                DidErrorCode.INVALID_DID_STRING);
      }
    }
  }

  private String getNetworkName(){
    return null;
  }
}
