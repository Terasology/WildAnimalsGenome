// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ServerEvent;

/**
 * This event is sent when an animal responds to a {@link MatingProposalEvent}.
 */
@ServerEvent
public class MatingProposalResponseEvent implements Event {
    /**
     * The animal sending the response.
     */
    @Replicate
    public EntityRef instigator;

    /**
     * The animal to whom the response is being sent.
     */
    @Replicate
    public EntityRef target;

    /**
     * Whether the mating request has been accepted or not.
     */
    @Replicate
    public boolean accepted;

    public MatingProposalResponseEvent(EntityRef instigator, EntityRef target, boolean accepted) {
        this.instigator = instigator;
        this.target = target;
        this.accepted = accepted;
    }

    public MatingProposalResponseEvent() {
    }
}
