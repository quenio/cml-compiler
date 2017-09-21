package cml.language.expressions;

import cml.language.types.NamedType;
import cml.language.types.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;

public class Unary extends ExpressionBase
{
    private final Collection<String> LOGIC_OPERATORS = unmodifiableCollection(singletonList(
        "not" // boolean operators
    ));

    private static Map<String, String> OPERATIONS =
        new HashMap<String, String>()
        {{
            put("+", "add");
            put("-", "sub");
            put("not", "not");
        }};

    private final String operator;
    private final Expression subExpr;

    public Unary(String operator, Expression subExpr)
    {
        super(singletonList(subExpr));

        this.operator = operator;
        this.subExpr = subExpr;
    }

    public String getOperator()
    {
        return operator;
    }

    public Optional<String> getOperation()
    {
        final String operation = OPERATIONS.get(getOperator());

        return Optional.ofNullable(operation);
    }

    public Expression getSubExpr()
    {
        return subExpr;
    }

    @Override
    public String getKind()
    {
        return "unary";
    }

    @Override
    public Type getType()
    {
        return LOGIC_OPERATORS.contains(getOperator()) ? NamedType.BOOLEAN : subExpr.getType();
    }
}
