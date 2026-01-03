package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.parts.PartHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MergeExecutor {

    private final MergeLogger logger;
    private final MergeWriter mergeWriter;

    public MergeExecutor(MergeLogger logger, MergeWriter mergeWriter) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.mergeWriter = Objects.requireNonNull(mergeWriter, "mergeWriter");
    }

    public void execute(List<PartHandle> parts) {
        if (parts == null || parts.isEmpty()) return;

        List<PartHandle> locked = new ArrayList<>();

        logger.log(new MergeEvent(
                MergeEvent.Type.STARTED,
                parts.stream().map(PartHandle::getPart).toList(),
                "merge started",
                null
        ));

        try {
            for (PartHandle h : parts) {
                if (!h.tryMarkMerging()) {
                    for (PartHandle l : locked) {
                        l.releaseMerging();
                    }
                    logger.log(new MergeEvent(
                            MergeEvent.Type.SKIPPED,
                            List.of(h.getPart()),
                            "merge skipped: part not active",
                            null
                    ));
                    return;
                }
                locked.add(h);
            }

            logger.log(new MergeEvent(
                    MergeEvent.Type.PLANNED,
                    parts.stream().map(PartHandle::getPart).toList(),
                    "merge locks acquired",
                    null
            ));

            mergeWriter.merge(parts.stream().map(PartHandle::getPart).toList());

            for (PartHandle h : parts) {
                h.markObsolete();
            }

            logger.log(new MergeEvent(
                    MergeEvent.Type.COMPLETED,
                    parts.stream().map(PartHandle::getPart).toList(),
                    "merge completed",
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