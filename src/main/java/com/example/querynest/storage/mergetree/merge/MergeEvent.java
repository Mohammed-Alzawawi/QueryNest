package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.parts.MergeTreePart;

import java.time.Instant;
import java.util.List;

public final class MergeEvent {

    public enum Type {
        PLANNED,
        STARTED,
        COMPLETED,
        FAILED,
        SKIPPED
    }

    private final Type type;
    private final Instant timestamp;
    private final List<MergeTreePart> sourceParts;
    private final String message;
    private final Throwable error;

    public MergeEvent(
            Type type,
            List<MergeTreePart> sourceParts,
            String message,
            Throwable error
    ) {
        this.type = type;
        this.timestamp = Instant.now();
        this.sourceParts = sourceParts;
        this.message = message;
        this.error = error;
    }

    public Type getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<MergeTreePart> getSourceParts() {
        return sourceParts;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getError() {
        return error;
    }
}