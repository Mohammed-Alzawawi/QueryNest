package com.example.querynest.storage.mergetree.cleanup;

import com.example.querynest.storage.mergetree.merge.MergeLogger;
import com.example.querynest.storage.mergetree.merge.MergeEvent;
import com.example.querynest.storage.mergetree.parts.PartHandle;
import com.example.querynest.storage.mergetree.util.FileSystemUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public final class ObsoletePartCleaner {

    private final MergeLogger logger;

    public ObsoletePartCleaner(MergeLogger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void clean(Collection<PartHandle> handles) {
        if (handles == null || handles.isEmpty()) return;

        for (PartHandle handle : handles) {
            if (!handle.isObsolete()) continue;

            try {
                FileSystemUtils.deleteRecursive(handle.getPart().getPath());

                logger.log(new MergeEvent(
                        MergeEvent.Type.COMPLETED,
                        java.util.List.of(handle.getPart()),
                        "obsolete part deleted",
                        null
                ));
            } catch (IOException e) {
                logger.log(new MergeEvent(
                        MergeEvent.Type.FAILED,
                        java.util.List.of(handle.getPart()),
                        "failed to delete obsolete part",
                        e
                ));
            }
        }
    }
}