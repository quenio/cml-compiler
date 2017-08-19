package cml.language.features;

import cml.language.foundation.ModelElementBase;
import cml.language.foundation.NamedElement;

public class Template extends ModelElementBase implements NamedElement
{
    private final Function function;

    public Template(final Function function)
    {
        this.function = function;
    }

    public Function getFunction()
    {
        return function;
    }

    @Override
    public String getName()
    {
        return function.getName();
    }
}