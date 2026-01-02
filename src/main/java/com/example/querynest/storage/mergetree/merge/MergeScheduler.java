package com.example.querynest.storage.mergetree.merge;

import com.example.querynest.storage.mergetree.MergeTreeConfig;
import com.example.querynest.storage.mergetree.parts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MergeScheduler implements Runnable {

    private final Path tableDir;
    private final ActivePartScanner scanner;
    private final PartValidator validator;
    private final PartOverlapResolver overlapResolver;
    private final MergePolicy policy;
    private final MergeSelector selector;
    private final PartHandleRegistry handleRegistry;
    private final MergeExecutor executor;
    private final MergeLogger logger;

    private volatile boolean running;
    private Thread thread;

    public MergeScheduler(
            Path tableDir,
            ActivePartScanner scanner,
            PartValidator validator,
            PartOverlapResolver overlapResolver,
            MergePolicy policy,
            MergeSelector selector,
            PartHandleRegistry handleRegistry,
            MergeExecutor executor,
            MergeLogger logger
    ) {
        this.tableDir = Objects.requireNonNull(tableDir, "tableDir");
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.validator = Objects.requireNonNull(validator, "validator");
        this.overlapResolver = Objects.requireNonNull(overlapResolver, "overlapResolver");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.selector = Objects.requireNonNull(selector, "selector");
        this.handleRegistry = Objects.requireNonNull(handleRegistry, "handleRegistry");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void start() {
        if (running) return;

        running = true;
        thread = new Thread(this, "merge-scheduler");
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<MergeTreePart> discovered = scanner.scanAllParts(tableDir);
                List<MergeTreePart> active = overlapResolver.resolveActive(discovered);

                ActivePartSnapshot snapshot = new ActivePartSnapshot(active, validator);

                if (!policy.shouldMerge(snapshot.getParts())) {
                    Thread.sleep(MergeTreeConfig.MERGE_INTERVAL_MS);
                    continue;
                }

                List<MergeTreePart> selected = selector.select(snapshot.getParts());
                if (selected.isEmpty()) {
                    Thread.sleep(MergeTreeConfig.MERGE_INTERVAL_MS);
                    continue;
                }

                List<PartHandle> handles = new ArrayList<>();
                for (MergeTreePart part : selected) {
                    handles.add(handleRegistry.getOrCreate(part));
                }

                executor.execute(handles);

                Thread.sleep(MergeTreeConfig.MERGE_INTERVAL_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                return;
            } catch (Exception e) {
                logger.log(new MergeEvent(
                        MergeEvent.Type.FAILED,
                        null,
                        "merge scheduler loop failed",
                        e
                ));
                try {
                    Thread.sleep(MergeTreeConfig.MERGE_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    running = false;
                    return;
                }
            }
        }
    }
}