// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

@ServerEvent
public class MatingCleanupEvent implements Event {
    @Replicate
    public EntityRef animal1;

    @Replicate
    public EntityRef animal2;

    public MatingCleanupEvent(EntityRef animal1, EntityRef animal2) {
        this.animal1 = animal1;
        this.animal2 = animal2;
    }

    public MatingCleanupEvent() {
    }
}
