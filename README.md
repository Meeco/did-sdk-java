# did-sdk-java

Support for the Hedera Hashgraph DID Method on the Hedera JAVA SDK.

This repository contains the Javascript SDK for managing [DID Documents][did-core] using the Hedera Consensus Service.

did-sdk-java based on [did-sdk-js], so both of them contain similar methods and classes.

## Overview

Hedera Consensus Service (HCS) allows applications to share common channels to publish and resolve immutable and
verifiable messages. These messages are submitted to Topic. SDK creates and uses **Private DID Topic** on HCS for
publishing **DID Events Messages** to resolve and validate **DID Document**.

This SDK is designed to simplify :

- Creation and initialization of the DID registration on **HCS Restricted Topic**,
- Generation of decentralized identifiers for [Hedera DID Method][did-method-spec] and creation of DID documents,
- Create, update, revoke, deletion, and resolution of DID documents based
  on [DID Document Core Properties][did-core-prop] event/log messages recorded on **HCS Topic**
- Transferring ownership of DID identifier and DID Document to another party.

The SDK adheres to W3C standards to produce valid hedera:did and resolve it to DID Document. SDK also provides API to
create, update, revoke and delete different DID Events Messages that represent different properties of DID documents.

## Usage

### Dependency Declaration

#### Maven

```xml

<dependency>
    <groupId>com.hedera.hashgraph</groupId>
    <artifactId>did-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### Gradle

```gradle
implementation 'com.hedera.hashgraph:did-sdk-java:1.0.0'
```

## Setup Hedera Portal Account

- Register hedera portal Testnet account <https://portal.hedera.com/register>
- Login to portal <https://portal.hedera.com/?network=testnet>
- Obtain accountId & privateKey string value.

```json
{
  "operator": {
    "accountId": "0.0.xxxx",
    "publicKey": "...",
    "privateKey": "302.."
  }
}
```

- Following examples use accountId as `OPERATOR_ID` and privateKey string value as `OPERATOR_KEY` to submit DID Event
  Messages to HCS.

## Examples

Sample demo step by step JAVA Test example are available at [Demo Test Folder][demo-location]. Make sure to add
appropriate `testnet` account details in <b>`lib/src/test/resources/demo.config.properties`</b>

- OPERATOR_ID=0.0.xxxx
- OPERATOR_KEY=302...

## DID Generation & Registration

```shell
gradle clean demoTests --tests demo.DemoTest.register -i
```

After running `register` test of the demo test flow use printed out values to complete
the <b>`lib/src/test/resources/demo.config.properties`</b> configuration file.

- DID_IDENTIFIER=did:hedera:testnet:..._0.0.xxx
- DID_PRIVATE_KEY=302...

That's it! You are set to execute other demo test flows.

## DID Resolve

```shell
gradle clean demoTests --tests demo.DemoTest.resolve -i
```

## Create, Update and Revoke [DID Document Core Properties][did-core-prop]

## Service

```shell
# Create
gradle clean demoTests --tests demo.DemoTest.addService -i

# Update
gradle clean demoTests --tests demo.DemoTest.updateService -i

# Revoke
gradle clean demoTests --tests demo.DemoTest.revokeService -i
```

## Verification Method

```shell
# Create
gradle clean demoTests --tests demo.DemoTest.addVerificationMethod -i

# Update
gradle clean demoTests --tests demo.DemoTest.updateVerificationMethod -i

# Revoke
gradle clean demoTests --tests demo.DemoTest.revokeVerificationMethod -i
```

## Verification RelationShip - Authentication

```shell
# Create
gradle clean demoTests --tests demo.DemoTest.addVerificationRelationship -i

# Update
gradle clean demoTests --tests demo.DemoTest.updateVerificationRelationship -i

# Revoke
gradle clean demoTests --tests demo.DemoTest.revokeVerificationRelationship -i
```

## Change Ownership

### Change DID Ownership, works under the following **assumption**

- Current DID owner **transfers** registered DID PrivateKey to new owner using **secure channel**.
- New owner **performs change did owner operation** with existing owner registered DID PrivateKey and new owners
  PrivateKey.

### Change DID Ownership performs following tasks

- It **transfers** the ownership of **DIDDocument** and **HCS Topic**.
- It **updates** Topic **AdminKey** and **SubmitKey** by signing updateTopicTransaction with **both** existing owner
  PrivateKey and new owner PrivateKey
- It also **submits** Update DIDOwner **Event** to **HCS Topic** with new owner PublicKey. - of course singed by new
  owner PrivateKey
- Eventually, when **DID Document** get **resolved**, Update DIDOwner **Event** new owner PublicKey translates to DID
  Document **controller/#did-root-key**

```shell
gradle clean demoTests --tests demo.DemoTest.changeOwnership -i
```

## Delete DID Document

```shell
gradle clean demoTests --tests demo.DemoTest.delete -i
```

## Development

```sh
git clone git@github.com:hashgraph/did-sdk-java.git
```

Run build in dev mode (with sourcemap generation and following changes)

```sh
gradle clean build
```

## Tests

Run Unit Tests

```sh
gradle clean test

```

Run Integration Test

Open `lib/src/test/resources/demo.config.properties` file and update the following environment variables with
your `testnet` account details

```js
OPERATOR_ID = "0.0.xxxxxx";
OPERATOR_KEY = "302e02...";
```

```sh
 gradle clean integrationTests
```

## References

- <https://github.com/hashgraph/did-method>
- <https://github.com/hashgraph/hedera-sdk-java>
- <https://docs.hedera.com/hedera-api/>
- <https://www.hedera.com/>
- <https://www.w3.org/TR/did-core/>
- <https://www.w3.org/TR/vc-data-model/>

## License Information

Licensed under _license placeholder_.

[did-method-spec]: https://github.com/hashgraph/did-method

[did-core]: https://www.w3.org/TR/did-core/

[demo-location]: https://github.com/Meeco/did-sdk-java/blob/task/wip/lib/src/test/java/demo/DemoTest.java

[did-core-prop]: https://w3c.github.io/did-core/#core-properties

[did-sdk-js]: https://github.com/Meeco/did-sdk-js
