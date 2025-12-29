package com.example.querynest.parser.statement;

import com.example.querynest.ast.ColumnDefinition;
import com.example.querynest.ast.CreateTableStatement;
import com.example.querynest.ast.constraints.Constraint;
import com.example.querynest.ast.constraints.ForeignKeyConstraint;
import com.example.querynest.ast.constraints.PrimaryKeyConstraint;
import com.example.querynest.ast.constraints.UniqueConstraint;
import com.example.querynest.exception.ParseException;
import com.example.querynest.parser.lexer.TokenNavigator;
import com.example.querynest.parser.lexer.TokenType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateTableBodyParser {

    public CreateTableStatement parse(TokenNavigator navigator) {

        navigator.consume(TokenType.CREATE, "Expected CREATE");
        navigator.consume(TokenType.TABLE, "Expected TABLE");

        String tableName = navigator.consume(TokenType.IDENTIFIER, "Expected table name").value();

        navigator.consume(TokenType.LEFT_PAREN, "Expected '('");

        List<ColumnDefinition> columns = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();
        List<String> orderByColumns = new ArrayList<>();

        while (!navigator.check(TokenType.RIGHT_PAREN)) {
            if (isConstraint(navigator)) {
                constraints.add(parseConstraint(navigator));
            } else {
                columns.add(parseColumn(navigator));
            }

            if (!navigator.check(TokenType.RIGHT_PAREN)) {
                navigator.consume(TokenType.COMMA, "Expected comma");
            }
        }

        navigator.consume(TokenType.RIGHT_PAREN, "Expected ')'");

        if (navigator.match(TokenType.ORDER)) {
            navigator.consume(TokenType.BY, "Expected BY after ORDER");
            orderByColumns = parseOrderByColumns(navigator);
        }

        String engine = "FileSystem";
        if (navigator.match(TokenType.ENGINE)) {
            navigator.consume(TokenType.EQUAL, "Expected '=' after ENGINE");
            engine = navigator.consume(TokenType.IDENTIFIER, "Expected engine name").value();
        }

        return new CreateTableStatement(
                tableName,
                columns,
                constraints,
                orderByColumns,
                engine
        );
    }

    private boolean isConstraint(TokenNavigator navigator) {
        return navigator.check(TokenType.CONSTRAINT)
                || navigator.check(TokenType.PRIMARY)
                || navigator.check(TokenType.FOREIGN)
                || navigator.check(TokenType.UNIQUE);
    }

    private ColumnDefinition parseColumn(TokenNavigator navigator) {
        String name = navigator.consume(TokenType.IDENTIFIER, "Expected column name").value();
        String dataType = navigator.advance().value().toUpperCase();

        boolean isNullable = true;
        String defaultValue = null;

        if (navigator.check(TokenType.LEFT_PAREN)) {
            navigator.advance();
            navigator.consume(TokenType.NUMBER, "Expected size");
            navigator.consume(TokenType.RIGHT_PAREN, "Expected ')'");
        }

        if (navigator.match(TokenType.NOT)) {
            navigator.consume(TokenType.NULL, "Expected NULL after NOT");
            isNullable = false;
        }

        if (navigator.match(TokenType.DEFAULT)) {
            defaultValue = navigator.advance().value();
        }

        return new ColumnDefinition(name, dataType, isNullable, defaultValue);
    }

    private Constraint parseConstraint(TokenNavigator navigator) {
        String name = null;

        if (navigator.match(TokenType.CONSTRAINT)) {
            name = navigator.consume(TokenType.IDENTIFIER, "Expected constraint name").value();
        }

        if (navigator.match(TokenType.PRIMARY)) {
            navigator.consume(TokenType.KEY, "Expected KEY");
            return new PrimaryKeyConstraint(name, parseColumnList(navigator));
        }

        if (navigator.match(TokenType.FOREIGN)) {
            navigator.consume(TokenType.KEY, "Expected KEY");
            List<String> cols = parseColumnList(navigator);
            navigator.consume(TokenType.REFERENCES, "Expected REFERENCES");
            String refTable = navigator.consume(TokenType.IDENTIFIER, "Expected table name").value();
            List<String> refCols = parseColumnList(navigator);
            return new ForeignKeyConstraint(name, cols, refTable, refCols);
        }

        if (navigator.match(TokenType.UNIQUE)) {
            return new UniqueConstraint(name, parseColumnList(navigator));
        }

        throw new ParseException("Unknown constraint");
    }

    private List<String> parseColumnList(TokenNavigator navigator) {
        navigator.consume(TokenType.LEFT_PAREN, "Expected '('");

        List<String> cols = new ArrayList<>();
        do {
            cols.add(navigator.consume(TokenType.IDENTIFIER, "Expected column").value());
        } while (navigator.match(TokenType.COMMA));

        navigator.consume(TokenType.RIGHT_PAREN, "Expected ')'");

        return cols;
    }
    private List<String> parseOrderByColumns(TokenNavigator navigator) {
        List<String> cols = new ArrayList<>();
        do {
            cols.add(navigator.consume(TokenType.IDENTIFIER, "Expected column name in ORDER BY").value());
        } while (navigator.match(TokenType.COMMA));
        return cols;
    }
}
