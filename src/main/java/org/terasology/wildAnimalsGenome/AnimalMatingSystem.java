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
package org.terasology.wildAnimalsGenome;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.advancedBehaviors.UpdateBehaviorEvent;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.events.OnBreed;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.wildAnimalsGenome.component.MatingBehaviorComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.*;
import org.terasology.wildAnimalsGenome.ui.AnimalInteractionScreen;

import java.util.List;

/**
 * This system handles the mating search, requests/responses and updating the behavior,
 */
@RegisterSystem
public class AnimalMatingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private DelayManager delayManager;
    @In
    private EntityManager entityManager;
    @In
    private NUIManager nuiManager;
    @In
    private AssetManager assetManager;

    /**
     * Delay between consecutive searches for a mate.
     */
    private long matingSearchInterval = 1000L;

    /**
     * Radius within which to look for a mate.
     */
    private float searchRadius = 10f;

    /**
     * Squared distance below which the animal is said to have "reached" its target mating block.
     */
    private float maxDistanceSquared = 1.8f;
    private static final String MATING_SEARCH_ID = "WildAnimalsGenome:MatingSearch";

    private static final Logger logger = LoggerFactory.getLogger(AnimalMatingSystem.class);

    @Override
    public void update(float delta) {
        BehaviorTree mateBT = assetManager.getAsset("WildAnimalsGenome:mate", BehaviorTree.class).get();
        for (EntityRef entityRef : entityManager.getEntitiesWith(MatingBehaviorComponent.class)) {
            MatingComponent matingComponent = entityRef.getComponent(MatingComponent.class);
            if (entityRef.getComponent(BehaviorComponent.class).tree != mateBT) {
                entityRef.removeComponent(MatingBehaviorComponent.class);
                if (matingComponent.matingEntity != EntityRef.NULL && matingComponent.matingEntity.hasComponent(MatingBehaviorComponent.class)) {
                    matingComponent.matingEntity.removeComponent(MatingBehaviorComponent.class);
                    matingComponent.matingEntity.send(new UpdateBehaviorEvent());
                }
                entityRef.send(new UpdateBehaviorEvent());
            }

            if (matingComponent.inMatingProcess) {
                MinionMoveComponent minionMoveComponent = entityRef.getComponent(MinionMoveComponent.class);
                if (minionMoveComponent.target != null) {
                    Vector3f target = new Vector3f(minionMoveComponent.target.getX(), minionMoveComponent.target.getY(), minionMoveComponent.target.getZ());
                    if (entityRef.getComponent(LocationComponent.class).getWorldPosition().distanceSquared(target) <= maxDistanceSquared) {
                        matingComponent.reachedTarget = true;
                        entityRef.saveComponent(matingComponent);
                        entityRef.send(new MatingTargetReachedEvent());
                    }
                }
            }
        }
    }

    /**
     * Finds nearby potential mates and sends a {@link MatingProposalEvent}.
     */
    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onMatingSearch(DelayedActionTriggeredEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        if (matingComponent.readyToMate && !matingComponent.inMatingProcess) {
            List<EntityRef> nearbyAnimals = findNearbyAnimals(entityRef.getComponent(LocationComponent.class), searchRadius, entityRef.getComponent(WildAnimalComponent.class).name);
            List<EntityRef> animals = filterMatingActivatedAnimals(nearbyAnimals);
            for (EntityRef animal : animals) {
                if (!animal.equals(entityRef)) {
                    matingComponent.inMatingProcess = true;
                    entityRef.saveComponent(matingComponent);
                    entityRef.addOrSaveComponent(new MatingBehaviorComponent());
                    animal.send(new MatingProposalEvent(entityRef));
                }
            }
            delayManager.addDelayedAction(entityRef, MATING_SEARCH_ID, matingSearchInterval);
        }
    }

    /**
     * Schedules a {@link DelayedActionTriggeredEvent} to search for potential mates when a {@link MatingActivatedEvent}
     * is received.
     */
    @ReceiveEvent(components = {WildAnimalComponent.class})
    public void onMatingActivated(MatingActivatedEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        delayManager.addDelayedAction(entityRef, MATING_SEARCH_ID, matingSearchInterval);
    }

    /**
     * Responds to a mating request with a {@link MatingProposalResponseEvent}, accepting if the conditions are met.
     * Also sends an {@link UpdateBehaviorEvent} to update the behavior to "mate".
     */
    @ReceiveEvent
    public void onMatingProposalReceived(MatingProposalEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        if (matingComponent.readyToMate && !matingComponent.inMatingProcess) {
            event.instigator.send(new MatingProposalResponseEvent(entityRef, true));
            matingComponent.inMatingProcess = true;
            matingComponent.matingEntity = event.instigator;
            entityRef.saveComponent(matingComponent);
            entityRef.addComponent(new MatingBehaviorComponent());

            MinionMoveComponent actorMinionMoveComponent = entityRef.getComponent(MinionMoveComponent.class);
            actorMinionMoveComponent.target = null;

            entityRef.send(new UpdateBehaviorEvent());
        } else {
            event.instigator.send(new MatingProposalResponseEvent(entityRef, false));
        }
    }

    /**
     * Updates behavior to "mate" if an accepted {@link MatingProposalResponseEvent} is received.
     */
    @ReceiveEvent
    public void onMatingResponseReceived(MatingProposalResponseEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        if (event.accepted && matingComponent.readyToMate) {
            matingComponent.matingEntity = event.instigator;
            entityRef.send(new UpdateBehaviorEvent());
            logger.info("Mating between " + entityRef.getId() + " and " + event.instigator.getId());
        } else {
            matingComponent.inMatingProcess = false;
        }
        entityRef.saveComponent(matingComponent);
    }

    /**
     * Sends a {@link MatingInitiatedEvent} when the entity reaches its mating target block.
     */
    @ReceiveEvent
    public void onMatingTargetReached(MatingTargetReachedEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        EntityRef matingEntity = matingComponent.matingEntity;
        if (matingEntity.getComponent(MatingComponent.class).reachedTarget) {
            entityRef.send(new MatingInitiatedEvent(entityRef, matingEntity));
        }
    }

    /**
     * Spawns the offspring after breeding the parent animals and receiving an {@link OnBreed} event.
     */
    @ReceiveEvent
    public void onAnimalsBred(OnBreed event, EntityRef entityRef, WildAnimalComponent wildAnimalComponent) {
        logger.info(event.getOrganism1().getComponent(GenomeComponent.class).genes);
        logger.info(event.getOrganism2().getComponent(GenomeComponent.class).genes);
        logger.info(event.getOffspring().getComponent(GenomeComponent.class).genes);
        LocationComponent locationComponent = entityRef.getComponent(LocationComponent.class);
        Vector3f spawnPos = locationComponent.getWorldPosition();
        Vector3f offset = new Vector3f(locationComponent.getWorldDirection());
        offset.scale(2);
        spawnPos.add(offset);
        event.getOffspring().send(new CharacterTeleportEvent(spawnPos));
        event.getOffspring().send(new UpdateBehaviorEvent());

        cleanupAfterMating(event.getOrganism1(), event.getOrganism2());
    }

    /**
     * Changes behavior to "mate" on receiving an {@link UpdateBehaviorEvent}.
     */
    @ReceiveEvent(components = {MatingBehaviorComponent.class})
    public void onUpdateBehaviorMate(UpdateBehaviorEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        if (matingComponent.readyToMate && matingComponent.inMatingProcess) {
            event.consume();
            BehaviorComponent behaviorComponent = entityRef.getComponent(BehaviorComponent.class);
            behaviorComponent.tree = assetManager.getAsset("WildAnimalsGenome:mate", BehaviorTree.class).get();
            logger.info("Changed behavior to mate");
            entityRef.saveComponent(behaviorComponent);
        }
    }

    /**
     * After mating is complete, resets the variables that were changed in the {@link MatingComponent} during mating.
     *
     * @param animal1 The first parent.
     * @param animal2 The second parent.
     */
    private void cleanupAfterMating(EntityRef animal1, EntityRef animal2) {
        animal1.removeComponent(MatingBehaviorComponent.class);
        animal2.removeComponent(MatingBehaviorComponent.class);
        animal1.send(new UpdateBehaviorEvent());
        animal2.send(new UpdateBehaviorEvent());

        MatingComponent matingComponent1 = animal1.getComponent(MatingComponent.class);
        MatingComponent matingComponent1New = new MatingComponent();
        matingComponent1New.matingDisabled = matingComponent1.matingDisabled;
        animal1.saveComponent(matingComponent1New);

        MatingComponent matingComponent2 = animal2.getComponent(MatingComponent.class);
        MatingComponent matingComponent2New = new MatingComponent();
        matingComponent2New.matingDisabled = matingComponent2.matingDisabled;
        animal2.saveComponent(matingComponent2New);
    }

    /**
     * Opens the {@link AnimalInteractionScreen} on activating an animal.
     */
    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH, components = {WildAnimalComponent.class})
    public void onFrob(ActivateEvent event, EntityRef entityRef) {
        event.consume();
        AnimalInteractionScreen animalInteractionScreen = nuiManager.pushScreen("WildAnimalsGenome:animalInteractionScreen", AnimalInteractionScreen.class);
        animalInteractionScreen.setAnimalEntity(entityRef);
    }

    /**
     * Find nearby animals within a specified range.
     *
     * @param actorLocationComponent {@link LocationComponent} of the animal.
     * @param searchRadius           The radius within which to search for.
     * @param animalName             The name of the animal which is being searched.
     * @return A list of {@link EntityRef} of the nearby animals.
     */
    private List<EntityRef> findNearbyAnimals(LocationComponent actorLocationComponent, float searchRadius, String animalName) {
        List<EntityRef> animalsWithinRange = Lists.newArrayList();
        float maxDistanceSquared = searchRadius * searchRadius;
        Iterable<EntityRef> allAnimals = entityManager.getEntitiesWith(WildAnimalComponent.class);

        for (EntityRef animal : allAnimals) {
            LocationComponent animalLocationComponent = animal.getComponent(LocationComponent.class);
            if (animal.getComponent(AliveCharacterComponent.class) == null) {
                continue;
            }
            if (animalLocationComponent.getWorldPosition().distanceSquared(actorLocationComponent.getWorldPosition()) <= maxDistanceSquared) {
                if (animal.getComponent(WildAnimalComponent.class).name.equals(animalName)) {
                    animalsWithinRange.add(animal);
                }
            }
        }
        return animalsWithinRange;
    }

    /**
     * Filters the animals which have been activated for mating from a list of all potential mates.
     *
     * @param allAnimals List of all potential mates.
     * @return List of {@link EntityRef} of the filtered potential mates.
     */
    private List<EntityRef> filterMatingActivatedAnimals(List<EntityRef> allAnimals) {
        List<EntityRef> result = Lists.newArrayList();
        for (EntityRef animal : allAnimals) {
            if (animal.hasComponent(MatingComponent.class) && animal.getComponent(MatingComponent.class).readyToMate) {
                result.add(animal);
            }
        }
        return result;
    }
}
