// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is sent when mating is activated.
 */
@ServerEvent
public class MatingActivatedEvent implements Event {
    @Replicate
    public EntityRef entityRef;

    @Replicate
    public boolean isActivated;

    public MatingActivatedEvent(EntityRef entityRef, boolean isActivated) {
        this.entityRef = entityRef;
        this.isActivated = isActivated;
    }

    public MatingActivatedEvent() {
    }
}
