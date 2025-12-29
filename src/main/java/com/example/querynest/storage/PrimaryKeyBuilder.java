package com.example.querynest.storage;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.ast.constraints.PrimaryKeyConstraint;

import java.util.ArrayList;
import java.util.List;

public class PrimaryKeyBuilder {

    public static List<String> extractPrimaryKeyColumns(
            List<ColumnDefinition> columns,
            List<Constraint> constraints) {

        for (Constraint constraint : constraints) {
            if (constraint instanceof PrimaryKeyConstraint) {
                PrimaryKeyConstraint pk = (PrimaryKeyConstraint) constraint;
                return new ArrayList<>(pk.columnNames());
            }
        }

        if (!columns.isEmpty()) {
            return List.of(columns.get(0).getName());
        }

        return List.of();
    }
}
