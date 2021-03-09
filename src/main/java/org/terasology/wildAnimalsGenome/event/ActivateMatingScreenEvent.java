// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

@OwnerEvent
public class ActivateMatingScreenEvent implements Event {
    private EntityRef targetEntity;

    public ActivateMatingScreenEvent() {}

    public ActivateMatingScreenEvent(EntityRef targetEntity) {
        this.targetEntity = targetEntity;
    }

    public EntityRef getTargetEntity() {
        return targetEntity;
    }
}
