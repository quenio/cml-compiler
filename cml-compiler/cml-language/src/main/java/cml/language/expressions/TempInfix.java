package cml.language.expressions;

import cml.language.generated.Expression;
import cml.language.generated.Infix;
import cml.language.generated.Type;
import cml.language.types.TempNamedType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static cml.language.functions.TypeFunctions.isBinaryFloatingPointWiderThan;
import static cml.language.functions.TypeFunctions.isNumericWiderThan;
import static cml.language.generated.UndefinedType.createUndefinedType;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableCollection;

public class TempInfix extends ExpressionBase implements Infix
{
    private static final String REFERENTIAL_EQUALITY = "===";
    private static final String REFERENTIAL_INEQUALITY = "!==";

    private static final Collection<String> ARITHMETIC_OPERATORS = unmodifiableCollection(asList(
        "+", "-", "*", "/", "%", "^"
    ));

    private static final Collection<String> RELATIONAL_OPERATORS = unmodifiableCollection(asList(
        "==", "!=", ">", ">=", "<", "<="
    ));

    private static final Collection<String> BOOLEAN_OPERATORS = unmodifiableCollection(asList(
        "and", "or", "xor", "implies"
    ));

    private static final Collection<String> REFERENTIAL_OPERATORS = unmodifiableCollection(asList(
        REFERENTIAL_EQUALITY, REFERENTIAL_INEQUALITY
    ));

    private static final Collection<String> STRING_OPERATORS = unmodifiableCollection(singletonList("&"));

    private static Map<String, String> OPERATIONS =
        new HashMap<String, String>()
        {{
            // Arithmetic Operators:
            put("+", "add");
            put("-", "sub");
            put("*", "mul");
            put("/", "div");
            put("%", "mod");
            put("^", "exp");

            // String Concatenation:
            put("&", "str_concat");

            // Relational Operators:
            put("==", "eq");
            put("!=", "not_eq");
            put(">", "gt");
            put(">=", "gte");
            put("<", "lt");
            put("<=", "lte");

            // Boolean Operators:
            put("and", "and");
            put("or", "or");
            put("xor", "xor");
            put("implies", "implies");

            // Referential Operators:
            put(REFERENTIAL_EQUALITY, "ref_eq");
            put(REFERENTIAL_INEQUALITY, "not_ref_eq");
        }};

    private final Infix infix;

    public TempInfix(String operator, Expression left, Expression right)
    {
        super(asList(left, right));

        infix = Infix.extendInfix(this, expression, expression, expression, expression, operator);
    }

    @Override
    public String getOperator()
    {
        return infix.getOperator();
    }

    @Override
    public Optional<String> getOperation()
    {
        final String operation = OPERATIONS.get(getOperator());

        return Optional.ofNullable(operation);
    }

    @Override
    public Expression getLeft()
    {
        return infix.getLeft();
    }

    @Override
    public Expression getRight()
    {
        return infix.getRight();
    }

    @Override
    public String getKind()
    {
        return "infix";
    }

    @Override
    public Type getType()
    {
        final Type leftType = getLeft().getType();
        final Type rightType = getRight().getType();

        assert leftType != null: "Left expression must have a type in order to be able to compute type of infix expression: " + getLeft().getKind();
        assert rightType != null: "Right expression must have a type in order to be able to compute type of infix expression: " + getRight().getKind();

        if (leftType.isUndefined())
        {
            return leftType;
        }
        else if (rightType.isUndefined())
        {
            return rightType;
        }
        else if (BOOLEAN_OPERATORS.contains(getOperator()) && leftType.isBoolean() && rightType.isBoolean())
        {
            return TempNamedType.BOOLEAN;
        }
        else if (RELATIONAL_OPERATORS.contains(getOperator()) && leftType.isRelational() && rightType.isRelational())
        {
            return TempNamedType.BOOLEAN;
        }
        else if (REFERENTIAL_OPERATORS.contains(getOperator()) && leftType.isReferential() && rightType.isReferential())
        {
            return TempNamedType.BOOLEAN;
        }
        else if (ARITHMETIC_OPERATORS.contains(getOperator()) && leftType.isNumeric() && rightType.isNumeric())
        {
            return isNumericWiderThan(leftType, rightType) ? leftType : rightType;
        }
        else if (ARITHMETIC_OPERATORS.contains(getOperator()) && leftType.isBinaryFloatingPoint() && rightType.isBinaryFloatingPoint())
        {
            return isBinaryFloatingPointWiderThan(leftType, rightType) ? leftType : rightType;
        }
        else if (STRING_OPERATORS.contains(getOperator()) && leftType.isPrimitive() && rightType.isPrimitive())
        {
            return TempNamedType.STRING;
        }
        else
        {
            return createUndefinedType(format(
                "Incompatible operand(s) for operator '%s':\n- left operand is '%s: %s'\n- right operand is '%s: %s'",
                getOperator(),
                getLeft().getDiagnosticId(), leftType.getDiagnosticId(),
                getRight().getDiagnosticId(), rightType.getDiagnosticId()));
        }
    }

    @Override
    public String getDiagnosticId()
    {
        return infix.getDiagnosticId();
    }
}