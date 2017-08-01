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
package org.terasology.wildAnimalsGenome.component;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.Replicate;

/**
 * This component allows WildAnimals to mate.
 */
public class MatingComponent implements Component {
    /**
     * Whether mating is disabled for the current animal.
     */
    @Replicate
    public boolean matingDisabled = false;

    /**
     * Whether the animal is ready/activated to mate.
     */
    @Replicate
    public boolean readyToMate = false;

    /**
     * Whether the animal is already in the process of mating.
     */
    @Replicate
    public boolean inMatingProcess = false;

    /**
     * Stores the entityRef to its current mate.
     */
    @Replicate
    public EntityRef matingEntity = EntityRef.NULL;

    /**
     * Stores the target block chosen where mating is to occur.
     */
    @Replicate
    public Vector3f target = null;

    /**
     * Whether the target mating block has been reached.
     */
    @Replicate
    public boolean reachedTarget = false;
}
