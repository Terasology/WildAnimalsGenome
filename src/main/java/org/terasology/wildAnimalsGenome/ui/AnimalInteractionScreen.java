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
package org.terasology.wildAnimalsGenome.ui;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.MatingActivatedEvent;

/**
 * System that handles the interaction screen for wild animals.
 */
public class AnimalInteractionScreen extends CoreScreenLayer {

    private UIButton mateButton;
    private EntityRef animalEntity;

    @Override
    public void initialise() {
        mateButton = find("mateButton", UIButton.class);
        mateButton.subscribe(button -> {
            MatingComponent matingComponent = animalEntity.getComponent(MatingComponent.class);
            if (matingComponent.readyToMate) {
                matingComponent.readyToMate = false;
                mateButton.setText("Activate mating");
            } else {
                matingComponent.readyToMate = true;
                animalEntity.send(new MatingActivatedEvent());
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
