package cml.language.foundation;

import cml.language.features.Concept;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableList;

public interface Type extends NamedElement
{
    Type UNDEFINED = Type.create("Undefined", null);
    Type BOOLEAN = Type.create("Boolean", null);

    Collection<String> PRIMITIVE_TYPE_NAMES = unmodifiableCollection(asList(
        "Boolean", "Integer", "Decimal", "String", "Regex", // main primitive types
        "Byte", "Short", "Long", "Float", "Double", "Char" // remaining primitive types
    ));

    List<String> ORDINAL_TYPE_NAMES = unmodifiableList(asList(
        "Byte", "Short", "Integer", "Long", "Float", "Double", "Decimal" // from smaller to largest
    ));

    String REQUIRED = "required";
    String OPTIONAL = "optional";
    String SET = "set";

    default boolean isPrimitive()
    {
        return PRIMITIVE_TYPE_NAMES.contains(getName());
    }

    default boolean isOrdinal()
    {
        return ORDINAL_TYPE_NAMES.contains(getName());
    }

    default boolean isGreaterThan(Type other)
    {
        assert this.isOrdinal() && other.isOrdinal()
            : "Both types must be ordinal in order to be compared: " + this.getName() + " & " + other.getName();

        return ORDINAL_TYPE_NAMES.indexOf(this.getName()) > ORDINAL_TYPE_NAMES.indexOf(other.getName());
    }

    Optional<String> getCardinality();

    Optional<Concept> getConcept();
    void setConcept(Concept module);

    default String getKind()
    {
        if (getCardinality().isPresent())
        {
            final String cardinality = getCardinality().get();

            if (cardinality.matches("\\?"))
            {
                return OPTIONAL;
            }
            else if (cardinality.matches("([*+])"))
            {
                return SET;
            }
        }

        return REQUIRED;
    }

    static Type create(String name)
    {
        return new TypeImpl(name, null);
    }

    static Type create(String name, @Nullable String cardinality)
    {
        return new TypeImpl(name, cardinality);
    }
}

class TypeImpl implements Type
{
    private final NamedElement namedElement;
    private final @Nullable String cardinality;

    private @Nullable Concept concept;

    TypeImpl(String name, @Nullable String cardinality)
    {
        this.namedElement = NamedElement.create(this, name);
        this.cardinality = cardinality;
    }

    @Override
    public Optional<Scope> getParentScope()
    {
        return namedElement.getParentScope();
    }

    @Override
    public String getName()
    {
        return namedElement.getName();
    }

    @Override
    public Optional<String> getCardinality()
    {
        return Optional.ofNullable(cardinality);
    }

    @Override
    public Optional<Concept> getConcept()
    {
        return Optional.ofNullable(concept);
    }

    @Override
    public void setConcept(@Nullable Concept concept)
    {
        this.concept = concept;
    }

    @Override
    public String toString()
    {
        return getName() + (getCardinality().isPresent() ? getCardinality().get() : "");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final TypeImpl other = (TypeImpl) o;
        return
            Objects.equals(this.getName(), other.getName()) &&
            Objects.equals(this.cardinality, other.cardinality);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cardinality);
    }
}

