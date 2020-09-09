// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.wildAnimalsGenome.event.ActivateMatingScreenEvent;
import org.terasology.wildAnimalsGenome.ui.AnimalInteractionScreen;

@RegisterSystem(RegisterMode.CLIENT)
public class AnimalMatingClientSystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;
    @In
    private LocalPlayer localPlayer;

    /**
     * Opens the {@link AnimalInteractionScreen} on activating an animal.
     */
    @ReceiveEvent
    public void onActivateAnimalInteractionScreenEvent(ActivateMatingScreenEvent event, EntityRef entity,
                                                       CharacterComponent component) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            AnimalInteractionScreen animalInteractionScreen = nuiManager.pushScreen("WildAnimalsGenome" +
                    ":animalInteractionScreen", AnimalInteractionScreen.class);
            animalInteractionScreen.setAnimalEntity(event.getTargetEntity());
        }
    }
}
