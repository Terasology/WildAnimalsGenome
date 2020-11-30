// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

/**
 * This event is sent when an animal sends a MatingProposal to a potential mate.
 */
@ServerEvent
public class MatingProposalEvent implements Event {
    /**
     * The animal sending the request.
     */
    @Replicate
    public EntityRef instigator;

    /**
     * The animal to which the request is sent.
     */
    @Replicate
    public EntityRef target;

    public MatingProposalEvent(EntityRef instigator, EntityRef target) {
        this.instigator = instigator;
        this.target = target;
    }

    public MatingProposalEvent() {
    }
}
