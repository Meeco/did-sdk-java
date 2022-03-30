package com.hedera.hashgraph.identity.hcs.did.event;

import com.hedera.hashgraph.identity.DidMethodOperation;
import com.hedera.hashgraph.identity.hcs.did.event.document.HcsDidDeleteEvent;
import com.hedera.hashgraph.identity.hcs.did.event.owner.HcsDidCreateDidOwnerEvent;
import com.hedera.hashgraph.identity.hcs.did.event.verificationRelationship.HcsDidCreateVerificationRelationshipEvent;
import com.hedera.hashgraph.identity.utils.Hashing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HcsDidEventParserTest {

    @Test
    void parseToHcsDidDeleteEventObj() {

        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.DELETE, null);
        Assertions.assertInstanceOf(HcsDidDeleteEvent.class, result);

    }

    @Test
    void itReturnsNullIfOperationInvalid() {

        HcsDidEvent result = HcsDidEventParser.fromBase64(null, null);
        Assertions.assertNull(result);

    }

    @Test
    void itReturnNullIfTargetNameNofFound() {
        String eventBase64 = Hashing.Base64.encode("{\"data\":\"data\"}");
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, eventBase64);
        Assertions.assertNull(result);

    }

    @Test
    void itReturnNullIfInvalidData() {
        String eventBase64 = Hashing.Base64.encode("invalid");
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, eventBase64);
        Assertions.assertNull(result);

    }

    @Test
    void itReturnNullIfTargetIsNull() {
        String eventBase64 = Hashing.Base64.encode("{\"Service\":\"null\"}");
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, eventBase64);
        Assertions.assertNull(result);

    }

    @Test
    void itReturnNullIfTargetDataEmpty() {
        String eventBase64 = Hashing.Base64.encode("{\"Service\":\"{}\"}");
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, eventBase64);
        Assertions.assertNull(result);

    }

    @Test
    void parseToHcsDidCreateVerificationRelationshipEventObj() {

        // HcsDidCreateVerificationRelationshipEvent
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, "eyJWZXJpZmljYXRpb25SZWxhdGlvbnNoaXAiOnsiaWQiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa2tjbjFFRFhjNXZ6cG12blFlQ0twRXN3eXJuUUc3cXE1OWs5MmdGUm0xRUdrXzAuMC4yOTYxNzgwMSNrZXktMSIsInJlbGF0aW9uc2hpcFR5cGUiOiJhdXRoZW50aWNhdGlvbiIsInR5cGUiOiJFZDI1NTE5VmVyaWZpY2F0aW9uS2V5MjAxOCIsImNvbnRyb2xsZXIiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa3B4WnRocXBSd0VQVG84YXBmVThYSnNvQ0pxNjlodEdva2pCUVNHVzE4QXlkXzAuMC4zMDgxODIxNyIsInB1YmxpY0tleU11bHRpYmFzZSI6Ino2TWtrY24xRURYYzV2enBtdm5RZUNLcEVzd3lyblFHN3FxNTlrOTJnRlJtMUVHayJ9fQ==");
        Assertions.assertInstanceOf(HcsDidCreateVerificationRelationshipEvent.class, result);
    }

    @Test
    void parseToHcsDidCreateDidOwnerEventObj() {

        // HcsDidCreateDidOwnerEvent
        HcsDidEvent result = HcsDidEventParser.fromBase64(DidMethodOperation.CREATE, "eyJESURPd25lciI6eyJpZCI6ImRpZDpoZWRlcmE6dGVzdG5ldDp6Nk1rcHhadGhxcFJ3RVBUbzhhcGZVOFhKc29DSnE2OWh0R29rakJRU0dXMThBeWRfMC4wLjMwODE4MjE3I2RpZC1yb290LWtleSIsInR5cGUiOiJFZDI1NTE5VmVyaWZpY2F0aW9uS2V5MjAxOCIsImNvbnRyb2xsZXIiOiJkaWQ6aGVkZXJhOnRlc3RuZXQ6ejZNa3B4WnRocXBSd0VQVG84YXBmVThYSnNvQ0pxNjlodEdva2pCUVNHVzE4QXlkXzAuMC4zMDgxODIxNyIsInB1YmxpY0tleU11bHRpYmFzZSI6Ino2TWtweFp0aHFwUndFUFRvOGFwZlU4WEpzb0NKcTY5aHRHb2tqQlFTR1cxOEF5ZCJ9fQ==");
        Assertions.assertInstanceOf(HcsDidCreateDidOwnerEvent.class, result);
    }


}
