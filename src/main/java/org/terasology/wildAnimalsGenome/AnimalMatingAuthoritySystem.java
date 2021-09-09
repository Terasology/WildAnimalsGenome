// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.behavior.BehaviorComponent;
import org.terasology.engine.logic.behavior.asset.BehaviorTree;
import org.terasology.engine.logic.characters.AliveCharacterComponent;
import org.terasology.engine.logic.characters.CharacterTeleportEvent;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.genome.events.OnBreed;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.wildAnimalsGenome.component.MatingBehaviorComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.ActivateMatingScreenEvent;
import org.terasology.wildAnimalsGenome.event.MatingActivatedEvent;
import org.terasology.wildAnimalsGenome.event.MatingCleanupEvent;
import org.terasology.wildAnimalsGenome.event.MatingInitiatedEvent;
import org.terasology.wildAnimalsGenome.event.MatingProposalEvent;
import org.terasology.wildAnimalsGenome.event.MatingProposalResponseEvent;
import org.terasology.wildAnimalsGenome.event.MatingTargetReachedEvent;

import java.util.List;

/**
 * This system handles the mating search, requests/responses and updating the behavior,
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AnimalMatingAuthoritySystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final String MATING_SEARCH_ID = "WildAnimalsGenome:MatingSearch";
    private static final Logger logger = LoggerFactory.getLogger(AnimalMatingAuthoritySystem.class);

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

    private static final Logger LOGGER = LoggerFactory.getLogger(AnimalMatingAuthoritySystem.class);

    @Override
    public void update(float delta) {
        BehaviorTree mateBT = assetManager.getAsset("WildAnimalsGenome:matingCritter", BehaviorTree.class).get();
        for (EntityRef entityRef : entityManager.getEntitiesWith(MatingBehaviorComponent.class)) {
            MatingComponent matingComponent = entityRef.getComponent(MatingComponent.class);
            if (entityRef.hasComponent(BehaviorComponent.class) && entityRef.getComponent(BehaviorComponent.class).tree != mateBT) {
                if (matingComponent.matingEntity != EntityRef.NULL && matingComponent.matingEntity.hasComponent(MatingBehaviorComponent.class)) {
                    matingComponent.matingEntity.removeComponent(MatingBehaviorComponent.class);
                    entityRef.send(new MatingCleanupEvent(entityRef, matingComponent.matingEntity));
                }
            }

            if (matingComponent.inMatingProcess) {
                MinionMoveComponent minionMoveComponent = entityRef.getComponent(MinionMoveComponent.class);
                if (minionMoveComponent.target != null) {
                    Vector3f target = new Vector3f(minionMoveComponent.target.x(), minionMoveComponent.target.y(), minionMoveComponent.target.z());
                    if (entityRef.getComponent(LocationComponent.class).getWorldPosition(new Vector3f()).distanceSquared(target) <= maxDistanceSquared) {
                        matingComponent.reachedTarget = true;
                        entityRef.saveComponent(matingComponent);
                        entityRef.send(new MatingTargetReachedEvent(entityRef));
                    }
                }
            }
        }
    }

    /**
     * Finds nearby potential mates and sends a {@link MatingProposalEvent}.
     */
    @ReceiveEvent
    public void onMatingSearch(DelayedActionTriggeredEvent event, EntityRef clientEntity) {
        if (event.getActionId().startsWith(MATING_SEARCH_ID)) {
            EntityRef animalEntity = entityManager.getEntity(getEntityIDFromString(event.getActionId()));
            MatingComponent matingComponent = animalEntity.getComponent(MatingComponent.class);
            if (matingComponent.readyToMate && !matingComponent.inMatingProcess) {
                List<EntityRef> nearbyAnimals = findNearbyAnimals(
                    animalEntity.getComponent(LocationComponent.class), searchRadius, animalEntity.getComponent(WildAnimalComponent.class).name);
                List<EntityRef> animals = filterMatingActivatedAnimals(nearbyAnimals);
                for (EntityRef animal : animals) {
                    if (!animal.equals(animalEntity)) {
                        matingComponent.inMatingProcess = true;
                        animalEntity.saveComponent(matingComponent);
                        animalEntity.addOrSaveComponent(new MatingBehaviorComponent());
                        clientEntity.send(new MatingProposalEvent(animalEntity, animal));
                    }
                }
                delayManager.addDelayedAction(clientEntity, MATING_SEARCH_ID + ":" + animalEntity.getId(), matingSearchInterval);
            }
        }
    }

    /**
     * Schedules a {@link DelayedActionTriggeredEvent} to search for potential mates when a {@link MatingActivatedEvent}
     * is received.
     */
    @ReceiveEvent
    public void onMatingActivated(MatingActivatedEvent event, EntityRef clientEntity) {
        MatingComponent matingComponent = event.entityRef.getComponent(MatingComponent.class);
        if (matingComponent == null) {
            matingComponent = new MatingComponent();
        }
        if (event.isActivated) {
            matingComponent.readyToMate = true;
            delayManager.addDelayedAction(clientEntity, MATING_SEARCH_ID + ":" + event.entityRef.getId(), matingSearchInterval);
        } else {
            matingComponent.readyToMate = false;
        }
        event.entityRef.saveComponent(matingComponent);
    }

    /**
     * Responds to a mating request with a {@link MatingProposalResponseEvent}, accepting if the conditions are met.
     * Also sends an {@link UpdateBehaviorEvent} to update the behavior to "mate".
     */
    @ReceiveEvent
    public void onMatingProposalReceived(MatingProposalEvent event, EntityRef entityRef) {
        MatingComponent matingComponent = event.target.getComponent(MatingComponent.class);
        if (matingComponent.readyToMate && !matingComponent.inMatingProcess) {
            matingComponent.inMatingProcess = true;
            matingComponent.matingEntity = event.instigator;
            event.target.saveComponent(matingComponent);
            event.target.addComponent(new MatingBehaviorComponent());

            MinionMoveComponent actorMinionMoveComponent = event.target.getComponent(MinionMoveComponent.class);
            actorMinionMoveComponent.target = null;

            entityRef.send(new MatingProposalResponseEvent(event.target, event.instigator, true));
        } else {
            entityRef.send(new MatingProposalResponseEvent(event.target, event.instigator, false));
        }
    }

    /**
     * Updates behavior to "mate" if an accepted {@link MatingProposalResponseEvent} is received.
     */
    @ReceiveEvent
    public void onMatingResponseReceived(MatingProposalResponseEvent event, EntityRef entityRef) {
        MatingComponent matingComponent = event.target.getComponent(MatingComponent.class);
        if (event.accepted && matingComponent.readyToMate) {
            matingComponent.matingEntity = event.instigator;
            logger.info("Mating between " + event.target.getId() + " and " + event.instigator.getId());
        } else {
            matingComponent.inMatingProcess = false;
        }
        event.target.saveComponent(matingComponent);
    }

    /**
     * Sends a {@link MatingInitiatedEvent} when the entity reaches its mating target block.
     */
    @ReceiveEvent
    public void onMatingTargetReached(MatingTargetReachedEvent event, EntityRef entityRef) {
        MatingComponent matingComponent = event.animalEntity.getComponent(MatingComponent.class);
        EntityRef matingEntity = matingComponent.matingEntity;
        if (matingEntity.getComponent(MatingComponent.class).reachedTarget) {
            MatingInitiatedEvent matingInitiatedEvent = new MatingInitiatedEvent();
            matingInitiatedEvent.animal1 = event.animalEntity;
            matingInitiatedEvent.animal2 = matingEntity;
            event.animalEntity.send(matingInitiatedEvent);
        }
    }

    @ReceiveEvent
    public void onOffspringCreated(OnBreed event, EntityRef entityRef) {
        LocationComponent locationComponent = entityRef.getComponent(LocationComponent.class);
        Vector3f spawnPos = locationComponent.getWorldPosition(new Vector3f());
        Vector3f offset = locationComponent.getWorldDirection(new Vector3f());
        offset.mul(2);
        spawnPos.add(offset);
        event.getOffspring().send(new CharacterTeleportEvent(spawnPos));
        entityRef.send(new MatingCleanupEvent(event.getOrganism1(), event.getOrganism2()));
    }

    /**
     * After mating is complete, resets the variables that were changed in the {@link MatingComponent} during mating.
     */
    @ReceiveEvent
    public void cleanupAfterMating(MatingCleanupEvent event, EntityRef entityRef) {
        EntityRef animal1 = event.animal1;
        EntityRef animal2 = event.animal2;
        animal1.removeComponent(MatingBehaviorComponent.class);
        animal2.removeComponent(MatingBehaviorComponent.class);

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
     * Sends a {@link ActivateMatingScreenEvent} to the client activating an animal
     */
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = WildAnimalComponent.class)
    public void onFrob(ActivateEvent event, EntityRef entityRef) {
        event.getInstigator().send(new ActivateMatingScreenEvent(entityRef));
        event.consume();
    }

    /**
     * Find nearby animals within a specified range.
     *
     * @param actorLocationComponent {@link LocationComponent} of the animal.
     * @param radius The radius within which to search for.
     * @param animalName The name of the animal which is being searched.
     * @return A list of {@link EntityRef} of the nearby animals.
     */
    private List<EntityRef> findNearbyAnimals(LocationComponent actorLocationComponent, float radius, String animalName) {
        List<EntityRef> animalsWithinRange = Lists.newArrayList();
        float distanceSquared = radius * radius;
        Iterable<EntityRef> allAnimals = entityManager.getEntitiesWith(WildAnimalComponent.class);

        Vector3f actorPosition = actorLocationComponent.getWorldPosition(new Vector3f());
        Vector3f animalLocation = new Vector3f();
        for (EntityRef animal : allAnimals) {
            LocationComponent animalLocationComponent = animal.getComponent(LocationComponent.class);
            if (animal.getComponent(AliveCharacterComponent.class) == null) {
                continue;
            }
            animalLocationComponent.getWorldPosition(animalLocation);
            if (animalLocation.distanceSquared(actorPosition) <= distanceSquared) {
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

    private Long getEntityIDFromString(String delayEventID) {
        return Long.parseLong(delayEventID.substring(delayEventID.indexOf(':', delayEventID.indexOf(':') + 1) + 1));
    }
}
