// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.wildAnimalsGenome.util;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Random;


/**
 * This class is used for the initial siblings system.
 * It can be used to process any weighted (or probability-related) conditions.
 * TODO: analyze the pertinence of creating a similar mechanism on ES
 */
public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
