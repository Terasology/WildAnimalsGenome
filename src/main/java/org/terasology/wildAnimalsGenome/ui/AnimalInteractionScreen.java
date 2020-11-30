// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.nui.widgets.UIButton;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.MatingActivatedEvent;

/**
 * System that handles the interaction screen for wild animals.
 */
public class AnimalInteractionScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private UIButton mateButton;
    private EntityRef animalEntity;

    @Override
    public void initialise() {
        mateButton = find("mateButton", UIButton.class);
        mateButton.subscribe(button -> {
            MatingComponent matingComponent = animalEntity.getComponent(MatingComponent.class);
            if (matingComponent.readyToMate) {
                localPlayer.getClientEntity().send(new MatingActivatedEvent(animalEntity, false));
                mateButton.setText("Activate mating");
            } else {
                localPlayer.getClientEntity().send(new MatingActivatedEvent(animalEntity, true));
                mateButton.setText("Deactivate mating");
            }
        });
    }

    public void setAnimalEntity(EntityRef entityRef) {
        animalEntity = entityRef;
        if (animalEntity.hasComponent(MatingComponent.class)) {
            if (animalEntity.getComponent(MatingComponent.class).readyToMate) {
                mateButton.setText("Deactivate mating");
            } else {
                mateButton.setText("Activate mating");
            }
        } else {
            MatingComponent matingComponent = new MatingComponent();
            animalEntity.addComponent(matingComponent);
            mateButton.setText("Activate mating");
        }

        if (animalEntity.getComponent(MatingComponent.class).matingDisabled) {
            mateButton.setEnabled(false);
        }
    }
}
