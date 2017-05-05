package cml.language.expressions;

import cml.language.foundation.ModelElement;
import cml.language.foundation.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

public interface Transform extends ModelElement
{
    String getOperation();
    Optional<String> getSuffix();
    List<String> getVariables();
    Optional<Expression> getInit();
    Optional<Expression> getExpr();

    static Transform create(String operation)
    {
        return new TransformImpl(operation, null, emptyList(), null, null);
    }

    static Transform create(String operation, Expression expr)
    {
        return new TransformImpl(operation, null, emptyList(), null, expr);
    }

    static Transform create(String operation, String suffix, Expression expr)
    {
        return new TransformImpl(operation, suffix, emptyList(), null, expr);
    }

    static Transform create(String operation, List<String> variables, Expression expr)
    {
        return new TransformImpl(operation, null, variables, null, expr);
    }
}

class TransformImpl implements Transform
{
    private final ModelElement modelElement;

    private final String operation;
    private final @Nullable String suffix;
    private final List<String> variables;
    private final @Nullable Expression init;
    private final @Nullable Expression expr;

    TransformImpl(
        String operation,
        @Nullable String suffix,
        List<String> variables,
        @Nullable Expression init,
        @Nullable Expression expr)
    {
        this.modelElement = ModelElement.create(this);
        this.operation = operation;
        this.suffix = suffix;
        this.variables = new ArrayList<>(variables);
        this.init = init;
        this.expr = expr;
    }

    @Override
    public String getOperation()
    {
        return operation;
    }

    @Override
    public Optional<String> getSuffix()
    {
        return Optional.ofNullable(suffix);
    }

    @Override
    public Optional<Expression> getExpr()
    {
        return Optional.ofNullable(expr);
    }

    @Override
    public List<String> getVariables()
    {
        return new ArrayList<>(variables);
    }

    @Override
    public Optional<Expression> getInit()
    {
        return Optional.ofNullable(init);
    }

    @Override
    public Optional<Scope> getParentScope()
    {
        return modelElement.getParentScope();
    }
}
