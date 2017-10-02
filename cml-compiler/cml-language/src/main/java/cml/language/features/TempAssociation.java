package cml.language.features;

import cml.language.foundation.Diagnostic;
import cml.language.foundation.Invariant;
import cml.language.foundation.InvariantValidator;
import cml.language.foundation.TempProperty;
import cml.language.generated.*;
import cml.language.types.TempType;

import java.util.List;
import java.util.Optional;

import static cml.language.functions.ModelElementFunctions.selfTypeOf;
import static cml.language.functions.TypeFunctions.isEqualTo;
import static cml.language.generated.ModelElement.extendModelElement;
import static cml.language.generated.NamedElement.extendNamedElement;
import static cml.language.generated.Scope.extendScope;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public interface TempAssociation extends Association
{
    default Optional<AssociationEnd> getAssociationEnd(String conceptName, String propertyName)
    {
        return getAssociationEnds().stream()
                                   .filter(associationEnd -> associationEnd.getConceptName().equals(conceptName))
                                   .filter(associationEnd -> associationEnd.getPropertyName().equals(propertyName))
                                   .findFirst();
    }

    default List<TempType> getPropertyTypes()
    {
        return getAssociationEnds()
            .stream()
            .map(AssociationEnd::getAssociatedProperty)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(Property::getType)
            .map(t -> (TempType)t)
            .collect(toList());
    }

    default List<TempType> getReversedPropertyTypes()
    {
        return seq(getPropertyTypes()).reverse().toList();
    }

    @SuppressWarnings("unused")
    default boolean isOneToMany()
    {
        final List<AssociationEnd> ends = getAssociationEnds();
        final AssociationEnd end0 = ends.get(0);
        final AssociationEnd end1 = ends.get(1);

        return oneToMany(end0, end1) || oneToMany(end1, end0);
    }

    @SuppressWarnings("unused")
    default boolean isOneToOne()
    {
        final List<AssociationEnd> ends = getAssociationEnds();
        final AssociationEnd end0 = ends.get(0);
        final AssociationEnd end1 = ends.get(1);

        return oneToOne(end0, end1);
    }

    default Optional<TempProperty> getOneProperty()
    {
        final List<AssociationEnd> ends = getAssociationEnds();
        final AssociationEnd end0 = ends.get(0);
        final AssociationEnd end1 = ends.get(1);

        if (oneToMany(end0, end1))
        {
            return end0.getAssociatedProperty().map(p -> (TempProperty) p);
        }

        if (oneToMany(end1, end0))
        {
            return end1.getAssociatedProperty().map(p -> (TempProperty) p);
        }

        return Optional.empty();
    }

    default Optional<TempProperty> getManyProperty()
    {
        final List<AssociationEnd> ends = getAssociationEnds();
        final AssociationEnd end0 = ends.get(0);
        final AssociationEnd end1 = ends.get(1);

        if (oneToMany(end0, end1))
        {
            return end1.getAssociatedProperty().map(p -> (TempProperty) p);
        }

        if (oneToMany(end1, end0))
        {
            return end0.getAssociatedProperty().map(p -> (TempProperty) p);
        }

        return Optional.empty();
    }

    static boolean oneToMany(AssociationEnd end1, AssociationEnd end2)
    {
        assert end1.getAssociatedProperty().isPresent();
        assert end2.getAssociatedProperty().isPresent();

        return !end1.getAssociatedProperty().get().getType().isSequence() && end2.getAssociatedProperty().get().getType().isSequence();
    }

    static boolean oneToOne(AssociationEnd end1, AssociationEnd end2)
    {
        assert end1.getAssociatedProperty().isPresent();
        assert end2.getAssociatedProperty().isPresent();

        return end1.getAssociatedProperty().get().getType().isSingle() && end2.getAssociatedProperty().get().getType().isSingle();
    }

    static TempAssociation create(TempModule module, String name, Location location)
    {
        return new AssociationImpl(module, name, location);
    }

    static InvariantValidator<TempAssociation> invariantValidator()
    {
        return () -> asList(
            new AssociationMustHaveTwoAssociationEnds(),
            new AssociationEndTypesMustMatch()
        );
    }

}

class AssociationImpl implements TempAssociation
{
    private final Association association;

    AssociationImpl(TempModule module, String name, Location location)
    {
        final ModelElement modelElement = extendModelElement(this, module, location);
        final NamedElement namedElement = extendNamedElement(this, modelElement, name);
        final Scope scope = extendScope(this, modelElement, emptyList());
        this.association = Association.extendAssociation(this, modelElement, namedElement, scope);
    }

    @Override
    public Optional<Location> getLocation()
    {
        return association.getLocation();
    }

    @Override
    public Optional<Scope> getParent()
    {
        return association.getParent();
    }

    @Override
    public Optional<Model> getModel()
    {
        return association.getModel();
    }

    @Override
    public Optional<Module> getModule()
    {
        return association.getModule();
    }

    @Override
    public String getName()
    {
        return association.getName();
    }

    @Override
    public List<ModelElement> getMembers()
    {
        return association.getMembers();
    }

    @Override
    public List<AssociationEnd> getAssociationEnds()
    {
        return association.getAssociationEnds();
    }

    @Override
    public String toString()
    {
        return "association " + getName();
    }
}

class AssociationMustHaveTwoAssociationEnds implements Invariant<TempAssociation>
{
    @Override
    public boolean evaluate(TempAssociation self)
    {
        return self.getAssociationEnds().size() == 2;
    }

    @Override
    public Diagnostic createDiagnostic(TempAssociation self)
    {
        return new Diagnostic("association_must_have_two_association_ends", self, self.getAssociationEnds());
    }
}

class AssociationEndTypesMustMatch implements Invariant<TempAssociation>
{
    @Override
    public boolean evaluate(TempAssociation self)
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

        if (!end1.getAssociatedConcept().isPresent() || !end1.getAssociatedProperty().isPresent() ||
            !end2.getAssociatedConcept().isPresent() || !end2.getAssociatedProperty().isPresent())
        {
            return true;
        }

        final TempConcept firstConcept = end1.getAssociatedConcept().map(c -> (TempConcept) c).get();
        final TempConcept secondConcept = end2.getAssociatedConcept().map(c -> (TempConcept) c).get();
        final TempProperty firstProperty = end1.getAssociatedProperty().map(p -> (TempProperty) p).get();
        final TempProperty secondProperty = end2.getAssociatedProperty().map(p -> (TempProperty) p).get();

        return typesMatch(firstConcept, secondProperty) && typesMatch(secondConcept, firstProperty);
    }

    private static boolean typesMatch(TempConcept concept, TempProperty property)
    {
        return isEqualTo(property.getType().getElementType(), selfTypeOf(concept));
    }

    @Override
    public Diagnostic createDiagnostic(TempAssociation self)
    {
        return new Diagnostic("association_end_types_must_match", self, self.getAssociationEnds());
    }
}