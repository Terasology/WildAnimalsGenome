// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome;

import com.google.common.base.Function;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.genome.GenomeDefinition;
import org.terasology.genome.GenomeRegistry;
import org.terasology.genome.breed.BreedingAlgorithm;
import org.terasology.genome.breed.FavourableWeightedBreedingAlgorithm;
import org.terasology.genome.component.GenomeComponent;
import org.terasology.genome.events.OnBreed;
import org.terasology.genome.genomeMap.SeedBasedGenomeMap;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.wildAnimalsGenome.component.MatingComponent;
import org.terasology.wildAnimalsGenome.event.MatingInitiatedEvent;
import org.terasology.wildAnimalsGenome.util.RandomCollection;

import javax.annotation.Nullable;

/**
 * This system handles the integration of Genome with the mating system.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AnimalGeneticsSystem extends BaseComponentSystem {

    private static final String GENOME_REGISTRY_PREFIX = "WildAnimals:";

    private static final Double SIBLINGS_NORMAL_PROBABILITY = 95.0;
    private static final Double SIBLINGS_TWINS_PROBABILITY = 4.5;
    private static final Double SIBLINGS_TRIPLETS_PROBABILITY = 0.5;

    @In
    private GenomeRegistry genomeRegistry;
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;

    private BreedingAlgorithm breedingAlgorithm;

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
        String genomeID = GENOME_REGISTRY_PREFIX + event.animal1.getId() + ":" + event.animal2.getId();
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

        for (int i = 0; i <= this.getSiblings(); i++) {
            if (event.animal1.getParentPrefab().getName().equals("WildAnimals:deer")) {
                entityRef.send(new OnBreed(event.animal1, event.animal2, entityManager.create("WildAnimals:babyDeer")));
            } else {
                entityRef.send(new OnBreed(event.animal1, event.animal2, entityManager.create(event.animal1.getParentPrefab())));
            }
        }

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
        genomeMap.addSeedBasedProperty("speedMultiplier", 0, 0, 1, Float.class, breedingAlgorithm,
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

    /**
     * This method processes the number of siblings according to the following probability scale:
     * Chances of having non-identical twins: 4.5%
     * Chances of having non-identical triplets: 0.5%
     */
    private int getSiblings() {
        RandomCollection<Integer> rc = new RandomCollection<>();
        rc.add(SIBLINGS_NORMAL_PROBABILITY, 1);
        rc.add(SIBLINGS_TWINS_PROBABILITY, 2);
        rc.add(SIBLINGS_TRIPLETS_PROBABILITY, 3);
        return rc.next();
    }
}
