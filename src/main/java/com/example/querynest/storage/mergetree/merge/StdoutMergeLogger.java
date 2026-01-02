package com.example.querynest.storage.mergetree.merge;

public final class StdoutMergeLogger implements MergeLogger {

    @Override
    public void log(MergeEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append("[MERGE] ")
                .append(event.getType())
                .append(" @ ")
                .append(event.getTimestamp())
                .append(" | ");

        if (event.getSourceParts() != null) {
            sb.append("parts=");
            sb.append(event.getSourceParts());
            sb.append(" | ");
        }

        if (event.getMessage() != null) {
            sb.append(event.getMessage());
        }

        if (event.getError() != null) {
            sb.append(" | error=");
            sb.append(event.getError().getMessage());
        }

        System.out.println(sb.toString());
    }
}