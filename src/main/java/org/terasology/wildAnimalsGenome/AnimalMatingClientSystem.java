// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
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
    public void onActivateAnimalInteractionScreenEvent(ActivateMatingScreenEvent event, EntityRef entity, CharacterComponent component) {
        if (entity.equals(localPlayer.getCharacterEntity())) {
            AnimalInteractionScreen animalInteractionScreen = nuiManager.pushScreen("WildAnimalsGenome:animalInteractionScreen", AnimalInteractionScreen.class);
            animalInteractionScreen.setAnimalEntity(event.getTargetEntity());
        }
    }
}
