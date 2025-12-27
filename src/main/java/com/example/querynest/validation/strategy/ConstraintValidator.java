package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.ast.constraints.ForeignKeyConstraint;
import com.example.querynest.ast.constraints.PrimaryKeyConstraint;
import com.example.querynest.ast.constraints.UniqueConstraint;
import com.example.querynest.schema.SchemaRegistry;
import com.example.querynest.validation.ValidationResult;

import java.util.stream.Collectors;

public class ConstraintValidator implements ValidationStrategy {

    private final SchemaRegistry registry;

    public ConstraintValidator(SchemaRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {

        var columns = stmt.columns().stream()
                .map(c -> c.name().toLowerCase())
                .collect(Collectors.toSet());

        for (Constraint c : stmt.constraints()) {

            if (c instanceof PrimaryKeyConstraint pk) {
                pk.columnNames().forEach(col -> {
                    if (!columns.contains(col.toLowerCase()))
                        result.addError("Primary key refers to missing column: " + col);
                });
            }

            if (c instanceof UniqueConstraint uq) {
                uq.columnNames().forEach(col -> {
                    if (!columns.contains(col.toLowerCase()))
                        result.addError("Unique constraint refers to missing column: " + col);
                });
            }

            if (c instanceof ForeignKeyConstraint fk) {
                fk.columnNames().forEach(col -> {
                    if (!columns.contains(col.toLowerCase()))
                        result.addError("Foreign key refers to missing column: " + col);
                });

                if (!registry.tableExists(fk.referencedTable())) {
                    result.addError("Foreign key references non-existent table: " + fk.referencedTable());
                    return;
                }

                fk.referencedColumns().forEach(rcol -> {
                    if (!registry.columnExists(fk.referencedTable(), rcol)) {
                        result.addError("Foreign key references missing column: " + rcol);
                    }
                });
            }
        }
    }
}
