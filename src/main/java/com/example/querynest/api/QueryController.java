package com.example.querynest.api;

import com.example.querynest.api.dto.ErrorResponse;
import com.example.querynest.api.dto.ParseResponse;
import com.example.querynest.api.dto.QueryRequest;
import com.example.querynest.api.dto.ValidationResponse;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.exception.ValidationException;
import com.example.querynest.service.SchemaService;
import com.example.querynest.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryController {

    private final SchemaService schemaService;

    @PostMapping("/parse")
    public ParseResponse parse(@RequestBody QueryRequest request) {
        CreateTableStatement ast = schemaService.parseStatement(request.getQuery());
        return new ParseResponse("success", ast);
    }

    @PostMapping("/validate")
    public Object validate(@RequestBody QueryRequest request) {
        try {
            CreateTableStatement ast = schemaService.parseStatement(request.getQuery());
            ValidationResult vr = schemaService.processCreateStatement(request.getQuery());
            return new ValidationResponse("success", vr.getWarnings());
        } catch (ValidationException ve) {
            return new ErrorResponse("Validation failed", ve.getErrors());
        }
    }


    @PostMapping("/query")
    public Object query(@RequestBody QueryRequest request) {
        try {
            CreateTableStatement ast = schemaService.parseStatement(request.getQuery());
            ValidationResult vr = schemaService.processCreateStatement(request.getQuery());
            return new ParseResponse("success", ast);
        } catch (ValidationException ve) {
            return new ErrorResponse("Validation failed", ve.getErrors());
        }
    }
}
