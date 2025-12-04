package com.example.querynest.validation.strategy;

import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.validation.ValidationResult;
import com.example.querynest.validation.strategy.ValidationStrategy;

public class EngineValidator implements ValidationStrategy {

    @Override
    public void validate(CreateTableStatement stmt, ValidationResult result) {

        String engine = stmt.engine();

        if (engine == null || engine.isBlank()) {
            result.addError("Engine cannot be empty");
            return;
        }

        if (!engine.equalsIgnoreCase("FileSystem")) {
            result.addError("Unsupported engine: " + engine);
        }
    }
}
