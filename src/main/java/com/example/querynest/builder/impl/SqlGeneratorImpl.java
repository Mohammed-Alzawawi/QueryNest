package com.example.querynest.builder.impl;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.ast.constraints.ForeignKeyConstraint;
import com.example.querynest.ast.constraints.PrimaryKeyConstraint;
import com.example.querynest.ast.constraints.UniqueConstraint;
import com.example.querynest.builder.SqlGenerator;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SqlGeneratorImpl implements SqlGenerator {
    @Override
    public String generate(CreateTableStatement statement) {
        String cols = statement.columns().stream()
                .map(c -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(c.name()).append(" ").append(c.dataType());
                    sb.append(c.isNullable() ? "" : " NOT NULL");
                    if (c.defaultValue() != null) {
                        sb.append(" DEFAULT ").append(c.defaultValue());
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining(", "));

        String cons = statement.constraints().stream()
                .map(this::constraintToSql)
                .collect(Collectors.joining(", "));

        String middle = cons.isBlank() ? cols : cols + ", " + cons;
        return "CREATE TABLE " + statement.tableName() + " (" + middle + ");";
    }

    private String constraintToSql(Constraint c) {
        String cname = (c.name() != null && !c.name().isBlank()) ? "CONSTRAINT " + c.name() + " " : "";
        if (c instanceof PrimaryKeyConstraint pk) {
            return cname + "PRIMARY KEY (" + String.join(", ", pk.columnNames()) + ")";
        } else if (c instanceof UniqueConstraint uq) {
            return cname + "UNIQUE (" + String.join(", ", uq.columnNames()) + ")";
        } else if (c instanceof ForeignKeyConstraint fk) {
            return cname + "FOREIGN KEY (" + String.join(", ", fk.columnNames()) + ") REFERENCES "
                    + fk.referencedTable() + " (" + String.join(", ", fk.referencedColumns()) + ")";
        }
        return "";
    }
}
