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

    public CreateTableStatement parse(TokenNavigator nav) {

        nav.consume(TokenType.CREATE, "Expected CREATE");
        nav.consume(TokenType.TABLE, "Expected TABLE");

        String tableName = nav.consume(TokenType.IDENTIFIER, "Expected table name").value();

        nav.consume(TokenType.LEFT_PAREN, "Expected '('");

        List<ColumnDefinition> columns = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();
        List<String> orderByColumns = new ArrayList<>();

        while (!nav.check(TokenType.RIGHT_PAREN)) {
            if (isConstraint(nav)) {
                constraints.add(parseConstraint(nav));
            } else {
                columns.add(parseColumn(nav));
            }

            if (!nav.check(TokenType.RIGHT_PAREN)) {
                nav.consume(TokenType.COMMA, "Expected comma");
            }
        }

        nav.consume(TokenType.RIGHT_PAREN, "Expected ')'");

        if (nav.match(TokenType.ORDER)) {
            nav.consume(TokenType.BY, "Expected BY after ORDER");
            orderByColumns = parseOrderByColumns(nav);
        }

        String engine = "FileSystem";
        if (nav.match(TokenType.ENGINE)) {
            nav.consume(TokenType.EQUAL, "Expected '=' after ENGINE");
            engine = nav.consume(TokenType.IDENTIFIER, "Expected engine name").value();
        }

        return new CreateTableStatement(
                tableName,
                columns,
                constraints,
                orderByColumns,
                engine
        );
    }

    private boolean isConstraint(TokenNavigator nav) {
        return nav.check(TokenType.CONSTRAINT)
                || nav.check(TokenType.PRIMARY)
                || nav.check(TokenType.FOREIGN)
                || nav.check(TokenType.UNIQUE);
    }

    private ColumnDefinition parseColumn(TokenNavigator nav) {
        String name = nav.consume(TokenType.IDENTIFIER, "Expected column name").value();
        String dataType = nav.advance().value().toUpperCase();

        boolean isNullable = true;
        String defaultValue = null;

        if (nav.check(TokenType.LEFT_PAREN)) {
            nav.advance();
            nav.consume(TokenType.NUMBER, "Expected size");
            nav.consume(TokenType.RIGHT_PAREN, "Expected ')'");
        }

        if (nav.match(TokenType.NOT)) {
            nav.consume(TokenType.NULL, "Expected NULL after NOT");
            isNullable = false;
        }

        if (nav.match(TokenType.DEFAULT)) {
            defaultValue = nav.advance().value();
        }

        return new ColumnDefinition(name, dataType, isNullable, defaultValue);
    }

    private Constraint parseConstraint(TokenNavigator nav) {
        String name = null;

        if (nav.match(TokenType.CONSTRAINT)) {
            name = nav.consume(TokenType.IDENTIFIER, "Expected constraint name").value();
        }

        if (nav.match(TokenType.PRIMARY)) {
            nav.consume(TokenType.KEY, "Expected KEY");
            return new PrimaryKeyConstraint(name, parseColumnList(nav));
        }

        if (nav.match(TokenType.FOREIGN)) {
            nav.consume(TokenType.KEY, "Expected KEY");
            List<String> cols = parseColumnList(nav);
            nav.consume(TokenType.REFERENCES, "Expected REFERENCES");
            String refTable = nav.consume(TokenType.IDENTIFIER, "Expected table name").value();
            List<String> refCols = parseColumnList(nav);
            return new ForeignKeyConstraint(name, cols, refTable, refCols);
        }

        if (nav.match(TokenType.UNIQUE)) {
            return new UniqueConstraint(name, parseColumnList(nav));
        }

        throw new ParseException("Unknown constraint");
    }

    private List<String> parseColumnList(TokenNavigator nav) {
        nav.consume(TokenType.LEFT_PAREN, "Expected '('");

        List<String> cols = new ArrayList<>();
        do {
            cols.add(nav.consume(TokenType.IDENTIFIER, "Expected column").value());
        } while (nav.match(TokenType.COMMA));

        nav.consume(TokenType.RIGHT_PAREN, "Expected ')'");

        return cols;
    }
    private List<String> parseOrderByColumns(TokenNavigator nav) {
        List<String> cols = new ArrayList<>();
        do {
            cols.add(nav.consume(TokenType.IDENTIFIER, "Expected column name in ORDER BY").value());
        } while (nav.match(TokenType.COMMA));
        return cols;
    }
}
