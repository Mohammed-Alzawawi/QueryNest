package com.example.querynest.service;

import com.example.querynest.ast.SelectStatement;

public interface DmlService {
    SelectStatement parseSelect(String rawQuery);
}
