// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

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
