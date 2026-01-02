package com.example.querynest.storage.mergetree.parts;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class PartHandle {

    private final MergeTreePart part;
    private final AtomicReference<PartState> state =
            new AtomicReference<>(PartState.ACTIVE);

    public PartHandle(MergeTreePart part) {
        this.part = Objects.requireNonNull(part, "part");
    }

    public MergeTreePart getPart() {
        return part;
    }

    public PartState getState() {
        return state.get();
    }

    public boolean tryMarkMerging() {
        return state.compareAndSet(PartState.ACTIVE, PartState.MERGING);
    }

    public void releaseMerging() {
        state.compareAndSet(PartState.MERGING, PartState.ACTIVE);
    }

    public void markObsolete() {
        state.set(PartState.OBSOLETE);
    }

    public boolean isActive() {
        return state.get() == PartState.ACTIVE;
    }

    public boolean isMerging() {
        return state.get() == PartState.MERGING;
    }

    public boolean isObsolete() {
        return state.get() == PartState.OBSOLETE;
    }
}