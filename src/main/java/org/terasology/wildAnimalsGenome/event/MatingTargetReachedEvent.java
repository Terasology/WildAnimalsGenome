// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

/**
 * This event is sent when an animal reaches its mating target block.
 */
@ServerEvent
public class MatingTargetReachedEvent implements Event {
    @Replicate
    public EntityRef animalEntity;

    public MatingTargetReachedEvent(EntityRef animalEntity) {
        this.animalEntity = animalEntity;
    }

    public MatingTargetReachedEvent() {
    }
}
