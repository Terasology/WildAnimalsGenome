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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.advancedBehaviors.UpdateBehaviorEvent;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.events.OnBreed;
import org.terasology.logic.behavior.BehaviorComponent;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.wildAnimalsGenome.component.MatingBehaviorComponent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.ActivateMatingScreenEvent;
import org.terasology.wildAnimalsGenome.event.MatingCleanupEvent;
import org.terasology.wildAnimalsGenome.ui.AnimalInteractionScreen;

@RegisterSystem(RegisterMode.CLIENT)
public class AnimalMatingClientSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;
    @In
    private AssetManager assetManager;

    private static final Logger logger = LoggerFactory.getLogger(AnimalMatingClientSystem.class);

    /**
     * Opens the {@link AnimalInteractionScreen} on activating an animal.
     */
    @ReceiveEvent
    public void onActivateAnimalInteractionScreenEvent(ActivateMatingScreenEvent event, EntityRef entity, CharacterComponent component) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            AnimalInteractionScreen animalInteractionScreen = nuiManager.pushScreen("WildAnimalsGenome:animalInteractionScreen", AnimalInteractionScreen.class);
            animalInteractionScreen.setAnimalEntity(event.getTargetEntity());
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

        localPlayer.getCharacterEntity().send(new MatingCleanupEvent(event.getOrganism1(), event.getOrganism2()));
    }
}
