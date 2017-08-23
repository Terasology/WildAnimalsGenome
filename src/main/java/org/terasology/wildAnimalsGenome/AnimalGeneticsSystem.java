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

import com.google.common.base.Function;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.genome.GenomeDefinition;
import org.terasology.genome.GenomeRegistry;
import org.terasology.genome.breed.BreedingAlgorithm;
import org.terasology.genome.breed.FavourableWeightedBreedingAlgorithm;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.events.OnBreed;
import org.terasology.genome.genomeMap.SeedBasedGenomeMap;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.registry.In;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.MatingInitiatedEvent;
import org.terasology.world.WorldProvider;

import javax.annotation.Nullable;

/**
 * This system handles the integration of Genome with the mating system.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AnimalGeneticsSystem extends BaseComponentSystem {
    @In
    private GenomeRegistry genomeRegistry;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;

    private BreedingAlgorithm breedingAlgorithm;

    private static final String genomeRegistryPrefix = "WildAnimals:";

    @Override
    public void preBegin() {
        breedingAlgorithm = new FavourableWeightedBreedingAlgorithm(0, 0.6f);
    }

    /**
     * Encodes the properties into genes dynamically and sends a {@link OnBreed} event.
     *
     * @param event
     * @param entityRef
     * @param matingComponent
     */
    @ReceiveEvent
    public void onMatingStart(MatingInitiatedEvent event, EntityRef entityRef, MatingComponent matingComponent) {
        // Build a unique genomeID for insertion into the registry.
        String genomeID = genomeRegistryPrefix + event.animal1.getId() + ":" + event.animal2.getId();
        addPropertyMap(event.animal1, event.animal2, genomeID);

        if (!event.animal1.hasComponent(GenomeComponent.class)) {
            event.animal1.addComponent(new GenomeComponent());
        }

        if (!event.animal2.hasComponent(GenomeComponent.class)) {
            event.animal2.addComponent(new GenomeComponent());
        }

        Float speedMultiplier1 = event.animal1.getComponent(CharacterMovementComponent.class).speedMultiplier;
        Float speedMultiplier2 = event.animal2.getComponent(CharacterMovementComponent.class).speedMultiplier;

        GenomeComponent genomeComponent1 = event.animal1.getComponent(GenomeComponent.class);
        GenomeComponent genomeComponent2 = event.animal2.getComponent(GenomeComponent.class);

        genomeComponent1.genes = speedMultiplier1 >= speedMultiplier2 ? "B" : "A";
        genomeComponent2.genes = speedMultiplier1 >= speedMultiplier2 ? "A" : "B";

        genomeComponent1.genomeId = genomeID;
        genomeComponent2.genomeId = genomeID;

        event.animal1.saveComponent(genomeComponent1);
        event.animal2.saveComponent(genomeComponent2);

        EntityRef offspring;
        if (event.animal1.getParentPrefab().getName().equals("WildAnimals:deer")) {
            offspring = entityManager.create("WildAnimals:babyDeer");
        } else {
            offspring = entityManager.create(event.animal1.getParentPrefab());
        }
        entityRef.send(new OnBreed(event.animal1, event.animal2, offspring));
    }

    /**
     * Registers properties with a unique ID to the genomeRegistry.
     *
     * @param animal1
     * @param animal2
     * @param genomeID
     */
    private void addPropertyMap(EntityRef animal1, EntityRef animal2, String genomeID) {
        SeedBasedGenomeMap genomeMap = new SeedBasedGenomeMap(worldProvider.getSeed().hashCode());
        genomeMap.addSeedBasedProperty("speedMultiplier", 0, 0, 1, Float.class,
                new Function<String, Float>() {
                    @Nullable
                    @Override
                    public Float apply(@Nullable String input) {
                        Float speedMultiplier1 = animal1.getComponent(CharacterMovementComponent.class).speedMultiplier;
                        Float speedMultiplier2 = animal2.getComponent(CharacterMovementComponent.class).speedMultiplier;
                        if (input.charAt(0) == 'A') {
                            return speedMultiplier1 >= speedMultiplier2 ? speedMultiplier2 : speedMultiplier1;
                        } else {
                            return speedMultiplier1 >= speedMultiplier2 ? speedMultiplier1 : speedMultiplier2;
                        }
                    }
                });
        GenomeDefinition genomeDefinition = new GenomeDefinition(breedingAlgorithm, genomeMap);
        genomeRegistry.registerType(genomeID, genomeDefinition);

    }
}
