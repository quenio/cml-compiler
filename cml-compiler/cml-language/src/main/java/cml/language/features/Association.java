package cml.language.features;

import cml.language.foundation.*;
import cml.language.generated.Location;
import cml.language.generated.ModelElement;
import cml.language.generated.NamedElement;
import cml.language.generated.Scope;
import cml.language.types.Type;

import java.util.List;
import java.util.Optional;

import static cml.language.functions.ModelElementFunctions.selfTypeOf;
import static cml.language.generated.ModelElement.extendModelElement;
import static cml.language.generated.NamedElement.extendNamedElement;
import static cml.language.generated.Scope.extendScope;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public interface Association extends NamedElement, Scope
{
    default Optional<AssociationEnd> getAssociationEnd(String conceptName, String propertyName)
    {
        return getAssociationEnds().stream()
                                   .filter(associationEnd -> associationEnd.getConceptName().equals(conceptName))
                                   .filter(associationEnd -> associationEnd.getPropertyName().equals(propertyName))
                                   .findFirst();
    }

    default List<AssociationEnd> getAssociationEnds()
    {
        return getMembers().stream()
                           .filter(e -> e instanceof AssociationEnd)
                           .map(e -> (AssociationEnd)e)
                           .collect(toList());
    }

    default List<Type> getPropertyTypes()
    {
        return getAssociationEnds()
            .stream()
            .map(AssociationEnd::getProperty)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Property::getType)
            .collect(toList());
    }

    default List<Type> getReversedPropertyTypes()
    {
        return seq(getPropertyTypes()).reverse().toList();
    }

    @SuppressWarnings("unused")
    default boolean isOneToMany()
    {
        return oneToMany(getAssociationEnds().get(0), getAssociationEnds().get(1)) ||
               oneToMany(getAssociationEnds().get(1), getAssociationEnds().get(0));
    }

    @SuppressWarnings("unused")
    default boolean isOneToOne()
    {
        return oneToOne(getAssociationEnds().get(0), getAssociationEnds().get(1));
    }

    default Optional<Property> getOneProperty()
    {
        if (oneToMany(getAssociationEnds().get(0), getAssociationEnds().get(1)))
        {
            return getAssociationEnds().get(0).getProperty();
        }

        if (oneToMany(getAssociationEnds().get(1), getAssociationEnds().get(0)))
        {
            return getAssociationEnds().get(1).getProperty();
        }

        return Optional.empty();
    }

    default Optional<Property> getManyProperty()
    {
        if (oneToMany(getAssociationEnds().get(0), getAssociationEnds().get(1)))
        {
            return getAssociationEnds().get(1).getProperty();
        }

        if (oneToMany(getAssociationEnds().get(1), getAssociationEnds().get(0)))
        {
            return getAssociationEnds().get(0).getProperty();
        }

        return Optional.empty();
    }

    static boolean oneToMany(AssociationEnd end1, AssociationEnd end2)
    {
        assert end1.getProperty().isPresent();
        assert end2.getProperty().isPresent();

        return !end1.getProperty().get().getType().isSequence() && end2.getProperty().get().getType().isSequence();
    }

    static boolean oneToOne(AssociationEnd end1, AssociationEnd end2)
    {
        assert end1.getProperty().isPresent();
        assert end2.getProperty().isPresent();

        return end1.getProperty().get().getType().isSingle() && end2.getProperty().get().getType().isSingle();
    }

    static Association create(Module module, String name, List<AssociationEnd> associationEnds, Location location)
    {
        return new AssociationImpl(module, name, associationEnds, location);
    }

    static InvariantValidator<Association> invariantValidator()
    {
        return () -> asList(
            new AssociationMustHaveTwoAssociationEnds(),
            new AssociationEndTypesMustMatch()
        );
    }

}

class AssociationImpl implements Association
{
    private final ModelElement modelElement;
    private final NamedElement namedElement;
    private final Scope scope;

    AssociationImpl(Module module, String name, List<AssociationEnd> associationEnds, Location location)
    {
        this.modelElement = extendModelElement(this, module, location);
        this.namedElement = extendNamedElement(modelElement, name);
        this.scope = extendScope(this, modelElement, seq(associationEnds).map(end -> (ModelElement)end).toList());
    }

    @Override
    public Optional<Location> getLocation()
    {
        return modelElement.getLocation();
    }

    @Override
    public Optional<Scope> getParent()
    {
        return modelElement.getParent();
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
    public String toString()
    {
        return "association " + getName();
    }
}

class AssociationMustHaveTwoAssociationEnds implements Invariant<Association>
{
    @Override
    public boolean evaluate(Association self)
    {
        return self.getAssociationEnds().size() == 2;
    }

    @Override
    public Diagnostic createDiagnostic(Association self)
    {
        return new Diagnostic("association_must_have_two_association_ends", self, self.getAssociationEnds());
    }
}

class AssociationEndTypesMustMatch implements Invariant<Association>
{
    @Override
    public boolean evaluate(Association self)
    {
        if (self.getAssociationEnds().size() != 2)
        {
            return true;
        }

        final Optional<AssociationEnd> first = self.getAssociationEnds().stream().findFirst();
        final Optional<AssociationEnd> last = self.getAssociationEnds().stream().reduce((previous, next) -> next);

        if (!first.isPresent() || !last.isPresent())
        {
            return true;
        }

        final AssociationEnd end1 = first.get();
        final AssociationEnd end2 = last.get();

        if (!end1.getConcept().isPresent() || !end1.getProperty().isPresent() ||
            !end2.getConcept().isPresent() || !end2.getProperty().isPresent())
        {
            return true;
        }

        final Concept firstConcept = end1.getConcept().get();
        final Concept secondConcept = end2.getConcept().get();
        final Property firstProperty = end1.getProperty().get();
        final Property secondProperty = end2.getProperty().get();

        return typesMatch(firstConcept, secondProperty) && typesMatch(secondConcept, firstProperty);
    }

    private static boolean typesMatch(Concept concept, Property property)
    {
        return property.getType().getElementType().isEqualTo(selfTypeOf(concept));
    }

    @Override
    public Diagnostic createDiagnostic(Association self)
    {
        return new Diagnostic("association_end_types_must_match", self, self.getAssociationEnds());
    }
}
