// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ServerEvent;

/**
 * This event is sent when mating is to be initiated, after both animals reach their mating target block.
 */
@ServerEvent
public class MatingInitiatedEvent implements Event {
    @Replicate
    public EntityRef animal1;

    @Replicate
    public EntityRef animal2;
}
