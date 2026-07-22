package com.enthusiasmpvp.donornpcs;

public enum RefreshMode {
    NORMAL,
    RECONCILE,
    FORCE;

    public boolean isForce() {
        return this == FORCE;
    }

    public boolean isMaintenance() {
        return this != NORMAL;
    }
}
