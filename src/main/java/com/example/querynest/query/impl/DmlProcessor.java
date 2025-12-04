package com.example.querynest.query.impl;

import com.example.querynest.query.QueryProcessor;
import org.springframework.stereotype.Component;

@Component
public class DmlProcessor extends QueryProcessor {

    @Override
    protected Object doProcess(String query) {
        // select/insert/delete/update
        throw new UnsupportedOperationException("DML processing is not implemented yet");
    }
}
