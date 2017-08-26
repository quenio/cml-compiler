package cml.language.expressions;

import cml.language.features.Function;
import cml.language.features.FunctionParameter;
import cml.language.foundation.*;
import cml.language.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.stream.Collectors.toMap;
import static org.jooq.lambda.Seq.seq;

public interface Invocation extends Expression, NamedElement
{
    String MESSAGE__UNABLE_TO_FIND_FUNCTION_OF_INVOCATION = "Unable to find function of invocation: ";
    String MESSAGE__SHOULD_MATCH_NUMBER_OF_PARAMS_IN_FUNCTION = "Number of arguments in invocation should match the number of parameters in function: ";
    String MESSAGE__SHOULD_MATCH_PARAMETER_TYPE_IN_FUNCTION = "Argument type should match parameter type in function: ";

    List<Expression> getArguments();
    Map<String, Expression> getNamedArguments();

    default Map<FunctionParameter, Expression> getParameterizedArguments()
    {
        if (getFunction().isPresent())
        {
            return seq(getFunction().get().getParameters())
                .zip(getArguments())
                .collect(toMap(Tuple2::v1, Tuple2::v2));
        }
        else
        {
            return emptyMap();
        }
    }

    default Map<FunctionType, Lambda> getTypedLambdaArguments()
    {
        return seq(getParameterizedArguments()).filter(t -> t.v1.getType() instanceof FunctionType)
                                               .filter(t -> t.v2 instanceof Lambda)
                                               .map(t -> new Tuple2<>((FunctionType) t.v1.getType(), (Lambda) t.v2))
                                               .collect(toMap(Tuple2::v1, Tuple2::v2));
    }

    Optional<Function> getFunction();
    void setFunction(@NotNull Function function);

    default Type getType()
    {
        if (getFunction().isPresent())
        {
            final Type resultType = getFunction().get().getType();

            return getMatchingTypeOf(resultType);
        }
        else
        {
            return NamedType.createUndefined(MESSAGE__UNABLE_TO_FIND_FUNCTION_OF_INVOCATION + getName());
        }
    }

    default Type getMatchingTypeOf(final Type type)
    {
        assert getFunction().isPresent();

        final Function function = getFunction().get();

        if (function.getParameters().size() == getArguments().size())
        {
            if (type instanceof TupleType)
            {
                final TupleType tupleType = (TupleType) type;
                final Seq<TupleTypeElement> matchingElements = tupleType.getElements()
                                                                        .map(e -> new TupleTypeElement(getMatchingTypeOf(e.getType()), e.getName().orElse(null)));

                return new TupleType(matchingElements, tupleType.getCardinality().orElse(null));
            }
            else if (type.isParameter())
            {
                int paramIndex = function.getParamIndexOfMatchingType(type);

                if (paramIndex < getArguments().size())
                {
                    Type paramType = getArguments().get(paramIndex).getMatchingResultType();

                    if (paramType.isUndefined())
                    {
                        paramIndex = function.getParamIndexOfMatchingType(type, paramIndex);

                        if (paramIndex < getArguments().size())
                        {
                            paramType = getArguments().get(paramIndex).getMatchingResultType();
                        }
                    }

                    if (paramType instanceof TupleType && type instanceof MemberType)
                    {
                        final TupleType tupleType = (TupleType) paramType;
                        final MemberType memberType = (MemberType) type;

                        paramType = tupleType.getElementTypes()
                                             .get(memberType.getParamIndex())
                                             .orElse(NamedType.createUndefined(MESSAGE__SHOULD_MATCH_PARAMETER_TYPE_IN_FUNCTION + getName()));
                    }

                    return paramType.withCardinality(type.getCardinality().orElse(null));
                }
                else
                {
                    return NamedType.createUndefined(MESSAGE__SHOULD_MATCH_PARAMETER_TYPE_IN_FUNCTION + getName());
                }
            }
            else
            {
                return type;
            }
        }
        else
        {
            return NamedType.createUndefined(MESSAGE__SHOULD_MATCH_NUMBER_OF_PARAMS_IN_FUNCTION + getName());
        }
    }

    default Optional<Scope> getExpressionScopeFor(final Lambda lambda)
    {
        final Optional<Type> scopeType = lambda.getExpectedScopeType();

        if (scopeType.isPresent())
        {
            final Type matchingType = getMatchingTypeOf(scopeType.get());

            assert matchingType.getConcept().isPresent(): "Expected concept but found '" + matchingType + "' for lambda: " + lambda + " - " + scopeType.get();

            return Optional.of(matchingType.getConcept().get());
        }
        else
        {
            final LambdaScope lambdaScope = createLambdaScope();

            lambda.getTypedParameters()
                  .forEach((name, type) -> lambdaScope.addParameter(name, getMatchingTypeOf(type)));

            return Optional.of(lambdaScope);
        }
    }

    default LambdaScope createLambdaScope()
    {
        final LambdaScope lambdaScope = new LambdaScope();

        getModule().ifPresent(m -> m.addMember(lambdaScope));

        return lambdaScope;
    }

    @Override
    default boolean evaluateInvariants()
    {
        if (getFunction().isPresent())
        {
            final Function function = getFunction().get();

            if (function.getParameters().size() == getArguments().size())
            {
                return seq(getParameterizedArguments()).allMatch(t -> typeMatches(t.v1, t.v2));
            }
        }

        return false;
    }

    default boolean typeMatches(final FunctionParameter param, final Expression argument)
    {
        final Type paramType = param.getMatchingResultType();
        final Type argumentType = argument.getMatchingResultType();

        return !argumentType.isUndefined() && getMatchingTypeOf(paramType).isAssignableFrom(argumentType);
    }

    @Override
    default Diagnostic createDiagnostic()
    {
        final boolean pass = evaluateInvariants();

        assert !pass;

        return new Diagnostic(
            "matching_function_for_invocation",
            this,
            getDiagnosticMessage(),
            getDiagnosticParticipants());
    }

    default String getDiagnosticMessage()
    {
        final boolean pass = evaluateInvariants();

        assert !pass;

        if (getFunction().isPresent())
        {
            final Function function = getFunction().get();

            if (function.getParameters().size() == getArguments().size())
            {
                return MESSAGE__SHOULD_MATCH_PARAMETER_TYPE_IN_FUNCTION + getName();
            }
            else
            {
                return MESSAGE__SHOULD_MATCH_NUMBER_OF_PARAMS_IN_FUNCTION + getName();
            }
        }

        return MESSAGE__UNABLE_TO_FIND_FUNCTION_OF_INVOCATION + getName();
    }

    default List<? extends ModelElement> getDiagnosticParticipants()
    {
        final boolean pass = evaluateInvariants();

        assert !pass;

        if (getFunction().isPresent())
        {
            final Function function = getFunction().get();

            if (function.getParameters().size() == getArguments().size())
            {
                return seq(getParameterizedArguments()).filter(t -> !typeMatches(t.v1, t.v2))
                                                       .flatMap(Tuple2::toSeq)
                                                       .map(e -> (ModelElement)e)
                                                       .toList();
            }
        }

        return emptyList();
    }

    @Override
    default String getDiagnosticIdentification()
    {
        return format("%s -> %s", toString(), getType());
    }

    static Invocation create(String name, List<Expression> arguments)
    {
        return new InvocationImpl(name, arguments);
    }

    static Invocation create(String name, LinkedHashMap<String, Expression> namedArguments)
    {
        return new ParameterizedInvocation(name, namedArguments);
    }
}

class InvocationImpl implements Invocation
{
    private final ModelElement modelElement;
    private final NamedElement namedElement;
    private final Scope scope;

    private final List<Expression> arguments;

    private @Nullable Function function;

    InvocationImpl(String name, List<Expression> arguments)
    {
        modelElement = ModelElement.create(this);
        namedElement = NamedElement.create(modelElement, name);
        scope = Scope.create(this, modelElement);

        this.arguments = new ArrayList<>(arguments);

        this.arguments.forEach(this::addMember);
    }

    @Override
    public List<Expression> getArguments()
    {
        return unmodifiableList(arguments);
    }

    @Override
    public Map<String, Expression> getNamedArguments()
    {
        if (getFunction().isPresent())
        {
            return seq(getFunction().get().getParameters())
                .zip(getArguments())
                .collect(toMap(t -> t.v1().getName(), Tuple2::v2));
        }
        else
        {
            return emptyMap();
        }
    }

    @Override
    public Optional<Function> getFunction()
    {
        return Optional.ofNullable(function);
    }

    @Override
    public void setFunction(@NotNull Function function)
    {
        assert this.function == null;

        this.function = function;
    }

    @Override
    public String getKind()
    {
        return "invocation";
    }

    @Override
    public Optional<Location> getLocation()
    {
        return modelElement.getLocation();
    }

    @Override
    public void setLocation(@Nullable Location location)
    {
        modelElement.setLocation(location);
    }

    @Override
    public Optional<Scope> getParentScope()
    {
        return modelElement.getParentScope();
    }

    @Override
    public String getName()
    {
        return namedElement.getName();
    }

    @Override
    public List<ModelElement> getMembers()
    {
        return scope.getMembers();
    }

    @Override
    public void addMember(ModelElement member)
    {
        scope.addMember(member);
    }

    @Override
    public String toString()
    {
        return format("%s(%s)", getName(), seq(arguments).toString(", "));
    }
}

class ParameterizedInvocation implements Invocation
{
    private final ModelElement modelElement;
    private final NamedElement namedElement;
    private final Scope scope;

    private final LinkedHashMap<String, Expression> namedArguments;

    private @Nullable Function function;

    ParameterizedInvocation(String name, LinkedHashMap<String, Expression> namedArguments)
    {
        modelElement = ModelElement.create(this);
        namedElement = NamedElement.create(modelElement, name);
        scope = Scope.create(this, modelElement);

        this.namedArguments = new LinkedHashMap<>(namedArguments);

        this.namedArguments.values().forEach(this::addMember);
    }

    @Override
    public List<Expression> getArguments()
    {
        return new ArrayList<>(namedArguments.values());
    }

    @Override
    public Map<String, Expression> getNamedArguments()
    {
        return unmodifiableMap(namedArguments);
    }

    @Override
    public Optional<Function> getFunction()
    {
        return Optional.ofNullable(function);
    }

    @Override
    public void setFunction(@NotNull Function function)
    {
        assert this.function == null;

        this.function = function;
    }

    @Override
    public String getKind()
    {
        return "invocation";
    }

    @Override
    public Optional<Location> getLocation()
    {
        return modelElement.getLocation();
    }

    @Override
    public void setLocation(@Nullable Location location)
    {
        modelElement.setLocation(location);
    }

    @Override
    public Optional<Scope> getParentScope()
    {
        return modelElement.getParentScope();
    }

    @Override
    public String getName()
    {
        return namedElement.getName();
    }

    @Override
    public List<ModelElement> getMembers()
    {
        return scope.getMembers();
    }

    @Override
    public void addMember(ModelElement member)
    {
        scope.addMember(member);
    }

    @Override
    public String toString()
    {
        final Seq<String> namedArguments = seq(getNamedArguments()).map(t -> format("%s: %s", t.v1, t.v2));

        return format("%s(%s)", getName(), namedArguments.toString(", "));
    }
}
