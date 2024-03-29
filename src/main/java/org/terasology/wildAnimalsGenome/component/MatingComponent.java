// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.component;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This component allows WildAnimals to mate.
 */
public class MatingComponent implements Component<MatingComponent> {
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

    @Override
    public void copyFrom(MatingComponent other) {
        this.matingDisabled = other.matingDisabled;
        this.readyToMate = other.readyToMate;
        this.inMatingProcess = other.inMatingProcess;
        this.matingEntity = other.matingEntity;
        this.target = other.target;
        this.reachedTarget = other.reachedTarget;
    }
}
