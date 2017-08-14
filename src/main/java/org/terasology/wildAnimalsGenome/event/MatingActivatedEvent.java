/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.wildAnimalsGenome.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

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
