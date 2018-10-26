package com.vinay.model;

import java.util.Set;

public class Barrier {
    public enum Type {
        BUS, TUBE
    }

    public enum Direction {
        IN, OUT
    }

    private final String name;
    private final Set<Integer> zones;
    private final Type type;
    private final Direction direction;

    public Barrier(String name, Set<Integer> zones, Type type, Direction direction) {
    	this.name = name;
    	this.zones = zones;
        this.type = type;
        this.direction = direction;
    }
    
    public String getName() {
        return name;
    }

    public Set<Integer> getZones() {
        return zones;
    }

    public Type getType() {
        return type;
    }

    public Direction getDirection() {
        return direction;
    }
    
    @Override
    public String toString() {
        return "Barrier{zones=" + zones + ", name='" + name + '\'' + ", type=" + type +", direction=" + direction + "}";
    }
}
