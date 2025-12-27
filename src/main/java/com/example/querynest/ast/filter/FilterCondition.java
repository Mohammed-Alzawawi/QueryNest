package com.example.querynest.ast.filter;

public record FilterCondition( String column, Operator operator, Value values)   {
}
