// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.BehaviorNode;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.module.behaviors.components.MinionMoveComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;

/**
 * Updates the target field in the {@link MinionMoveComponent} of the animal's mate with the target set in the current animal's {@link
 * MinionMoveComponent}
 */
@BehaviorAction(name = "set_mating_target_block")
public class SetMatingTargetBlockNode extends BaseAction {
    @Override
    public void construct(Actor actor) {
        MatingComponent matingComponent = actor.getComponent(MatingComponent.class);
        EntityRef matingEntity = matingComponent.matingEntity;
        MatingComponent matingComponent1 = matingEntity.getComponent(MatingComponent.class);

        MinionMoveComponent actorMoveComponent = actor.getComponent(MinionMoveComponent.class);
        MinionMoveComponent matingEntityMoveComponent = matingEntity.getComponent(MinionMoveComponent.class);

        if (actorMoveComponent.target != null) {
            Vector3i actorTarget = actorMoveComponent.target;
            matingEntityMoveComponent.target = new Vector3i(actorTarget);
            matingEntityMoveComponent.target.add(1, 0, 0);
            matingEntity.saveComponent(matingEntityMoveComponent);

            matingComponent.target = new Vector3f(actorMoveComponent.target);
            matingComponent1.target = new Vector3f(matingEntityMoveComponent.target);
            actor.save(matingComponent);
            matingEntity.saveComponent(matingComponent1);
        }

    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState behaviorState) {
        MatingComponent matingComponent = actor.getComponent(MatingComponent.class);
        EntityRef matingEntity = matingComponent.matingEntity;
        MinionMoveComponent matingEntityMoveComponent = matingEntity.getComponent(MinionMoveComponent.class);
        if (matingEntityMoveComponent == null) {
            return BehaviorState.FAILURE;
        }

        if (matingEntityMoveComponent.target != null) {
            return BehaviorState.SUCCESS;
        }
        return BehaviorState.FAILURE;
    }

}


