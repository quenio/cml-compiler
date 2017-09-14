package paths.poj;

import java.util.*;
import java.math.*;
import org.jetbrains.annotations.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class ExpressionCases
{
    private final String foo;
    private final SomeConcept somePath;
    private final List<SomeConcept> somePathList;

    public ExpressionCases(String foo, SomeConcept somePath, List<SomeConcept> somePathList)
    {
        this.foo = foo;
        this.somePath = somePath;
        this.somePathList = somePathList;
    }

    public String getFoo()
    {
        return this.foo;
    }

    public SomeConcept getSomePath()
    {
        return this.somePath;
    }

    public List<SomeConcept> getSomePathList()
    {
        return Collections.unmodifiableList(this.somePathList);
    }

    public ExpressionCases getSelfVar()
    {
        return this;
    }

    public String getSingleVar()
    {
        return getFoo();
    }

    public int getPathVar()
    {
        return getSomePath()
                   .getBar();
    }

    public BigDecimal getPathVar2()
    {
        return getSomePath()
                   .getOneMorePath()
                   .getEtc();
    }

    public List<BigDecimal> getPathVar3()
    {
        return getSomePathList()
                   .stream()
                   .map(someConcept -> someConcept.getOneMorePath())
                   .map(anotherConcept -> anotherConcept.getEtc())
                   .collect(toList());
    }

    public List<Integer> getPathBars()
    {
        return getSomePathList()
                   .stream()
                   .map(someConcept -> someConcept.getBar())
                   .collect(toList());
    }

    public String toString()
    {
        return new StringBuilder(ExpressionCases.class.getSimpleName())
                   .append('(')
                   .append("foo=").append(String.format("\"%s\"", getFoo())).append(", ")
                   .append("somePath=").append(String.format("\"%s\"", getSomePath())).append(", ")
                   .append("singleVar=").append(String.format("\"%s\"", getSingleVar())).append(", ")
                   .append("pathVar=").append(String.format("\"%s\"", getPathVar())).append(", ")
                   .append("pathVar2=").append(String.format("\"%s\"", getPathVar2())).append(", ")
                   .append("pathVar3=").append(getPathVar3()).append(", ")
                   .append("pathBars=").append(getPathBars())
                   .append(')')
                   .toString();
    }
}