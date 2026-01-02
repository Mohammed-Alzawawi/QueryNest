package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.parts.PartHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MergeExecutor {

    private final MergeLogger logger;

    public MergeExecutor(MergeLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void execute(List<PartHandle> parts) {
        if (parts == null || parts.isEmpty()) return;

        List<PartHandle> locked = new ArrayList<>();

        logger.log(new MergeEvent(
                MergeEvent.Type.STARTED,
                parts.stream().map(PartHandle::getPart).toList(),
                "merge attempt started",
                null
        ));

        try {
            for (PartHandle handle : parts) {
                if (!handle.tryMarkMerging()) {
                    for (PartHandle l : locked) {
                        l.releaseMerging();
                    }

                    logger.log(new MergeEvent(
                            MergeEvent.Type.SKIPPED,
                            List.of(handle.getPart()),
                            "merge skipped: part already merging or not active",
                            null
                    ));
                    return;
                }
                locked.add(handle);
            }

            logger.log(new MergeEvent(
                    MergeEvent.Type.PLANNED,
                    parts.stream().map(PartHandle::getPart).toList(),
                    "merge locks acquired successfully (planning complete)",
                    null
            ));

            logger.log(new MergeEvent(
                    MergeEvent.Type.COMPLETED,
                    parts.stream().map(PartHandle::getPart).toList(),
                    "merge completed (no-op executor for now)",
                    null
            ));

        } catch (Exception e) {
            for (PartHandle l : locked) {
                l.releaseMerging();
            }

            logger.log(new MergeEvent(
                    MergeEvent.Type.FAILED,
                    parts.stream().map(PartHandle::getPart).toList(),
                    "merge failed",
                    e
            ));
        }
    }
}