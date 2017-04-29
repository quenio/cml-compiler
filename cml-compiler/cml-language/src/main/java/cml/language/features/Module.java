package cml.language.features;

import cml.language.foundation.ModelElement;
import cml.language.foundation.NamedElement;
import cml.language.foundation.Scope;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public interface Module extends NamedElement, Scope
{
    default List<Import> getImports()
    {
        return getElements().stream()
                            .filter(e -> e instanceof Import)
                            .map(e -> (Import)e)
                            .collect(toList());
    }

    default List<Module> getImportedModules()
    {
        return getImports().stream()
                           .map(Import::getModule)
                           .filter(Optional::isPresent)
                           .map(Optional::get)
                           .collect(toList());
    }

    default Optional<Module> getImportedModule(final String name)
    {
        for (final Module module: getImportedModules())
        {
            if (module.getName().equals(name))
            {
                return Optional.of(module);
            }
        }

        return Optional.empty();
    }

    default Optional<Module> getSelfOrImportedModule(final String name)
    {
        if (getName().equals(name))
        {
            return Optional.of(this);
        }

        return getImportedModule(name);
    }

    default List<Concept> getConcepts()
    {
        return getElements().stream()
                            .filter(e -> e instanceof Concept)
                            .map(e -> (Concept)e)
                            .collect(toList());
    }

    default List<Concept> getImportedConcepts()
    {
        return getImports().stream()
                        .map(Import::getModule)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(m -> m.getConcepts().stream())
                        .collect(toList());
    }

    default List<Concept> getAllConcepts()
    {
        return Stream.concat(
            getConcepts().stream(),
            getImportedConcepts().stream())
            .collect(toList());
    }

    default Optional<Concept> getConcept(String name)
    {
        return getAllConcepts().stream()
                               .filter(concept -> concept.getName().equals(name))
                               .findFirst();
    }

    default List<Task> getTasks()
    {
        return getElements().stream()
                            .filter(e -> e instanceof Task)
                            .map(e -> (Task)e)
                            .collect(toList());
    }

    static Module create(String name)
    {
        return new ModuleImpl(name);
    }
}

class ModuleImpl implements Module
{
    private final ModelElement modelElement;
    private final NamedElement namedElement;
    private final Scope scope;

    ModuleImpl(String name)
    {
        this.modelElement = ModelElement.create(this);
        this.namedElement = NamedElement.create(modelElement, name);
        this.scope = Scope.create(this, modelElement);
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
    public List<ModelElement> getElements()
    {
        return scope.getElements();
    }

    @Override
    public void addElement(ModelElement element)
    {
        scope.addElement(element);
    }
}
