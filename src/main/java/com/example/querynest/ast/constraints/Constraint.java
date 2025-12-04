package com.example.querynest.ast.constraints;

public sealed interface Constraint
        permits PrimaryKeyConstraint, ForeignKeyConstraint, UniqueConstraint {
    String name();
}