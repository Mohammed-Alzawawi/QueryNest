package com.example.querynest.service.impl;

import com.example.querynest.ast.SelectStatement;
import com.example.querynest.parser.lexer.Lexer;
import com.example.querynest.parser.lexer.Token;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.statement.SelectStatementParser;
import com.example.querynest.service.DmlService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DmlServiceImpl implements DmlService {
    private final Lexer lexer;
    private final SelectStatementParser selectParser;

    public DmlServiceImpl(Lexer lexer, SelectStatementParser selectParser) {
        this.lexer = lexer;
        this.selectParser = selectParser;
    }
    @Override
    public SelectStatement parseSelect(String rawQuery) {
        List<Token> tokens = lexer.tokenize(rawQuery);
        TokenNavigator nav = new TokenNavigator(tokens);
        return selectParser.parse(nav);
    }
}