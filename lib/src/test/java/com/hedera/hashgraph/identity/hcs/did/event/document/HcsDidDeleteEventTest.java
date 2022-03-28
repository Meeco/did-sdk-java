package com.hedera.hashgraph.identity.hcs.did.event.document;

import com.hedera.hashgraph.identity.hcs.did.event.HcsDidEventTargetName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HcsDidDeleteEventTest {

    HcsDidDeleteEvent event = new HcsDidDeleteEvent();

    @Test
    void itTargetsService() {
        assertEquals(event.getTargetName(), HcsDidEventTargetName.Document);
    }


    @Test
    void getId() {
        assertNull(event.getId());
    }

    @Test
    void toJsonTree() {
        assertNull(event.toJsonTree());
    }

    @Test
    void toJson() {
        assertEquals(event.toJSON(), "null");
    }

    @Test
    void getBase64() {
        assertNull(event.getBase64());
    }

    @Test
    void fromJsonTree() {
        assertTrue(HcsDidDeleteEvent.fromJsonTree(null) instanceof HcsDidDeleteEvent);
    }

}
