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
package org.terasology.wildAnimalsGenome.BehaviorNode;

import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
//import org.terasology.logic.behavior.tree.Node;
//import org.terasology.logic.behavior.tree.Status;
//import org.terasology.logic.behavior.tree.Task;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;

/**
 * Updates the target field in the {@link MinionMoveComponent} of the animal's mate with the target set in the current
 * animal's {@link MinionMoveComponent}
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
            Vector3f actorTarget = actorMoveComponent.target;
            matingEntityMoveComponent.target = new Vector3f(actorTarget);
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
        if(matingEntityMoveComponent == null)
            return BehaviorState.FAILURE;

        if (matingEntityMoveComponent.target != null) {
            return BehaviorState.SUCCESS;
        }
        return BehaviorState.FAILURE;
    }

}


