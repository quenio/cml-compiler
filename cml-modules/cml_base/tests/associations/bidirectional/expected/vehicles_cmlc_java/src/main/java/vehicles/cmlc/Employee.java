package vehicles.cmlc;

import java.util.*;
import java.math.*;
import org.jetbrains.annotations.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public interface Employee
{
    String getName();

    Organization getEmployer();

    static Employee createEmployee(String name, Organization employer)
    {
        return new EmployeeImpl(name, employer);
    }

    static Employee extendEmployee(String name, Organization employer)
    {
        return new EmployeeImpl(name, employer);
    }
}

class EmployeeImpl implements Employee
{
    private static Employment employment;

    private final String name;

    public EmployeeImpl(String name, Organization employer)
    {
        this.name = name;

        employment.link(employer, this);
    }

    public String getName()
    {
        return this.name;
    }

    public Organization getEmployer()
    {
        return employment.employerOf(this).get();
    }

    public String toString()
    {
        return new StringBuilder(Employee.class.getSimpleName())
                   .append('(')
                   .append("name=").append(String.format("\"%s\"", getName()))
                   .append(')')
                   .toString();
    }

    static void setEmployment(Employment association)
    {
        employment = association;
    }

    static
    {
        Employment.init(Employee.class);
    }
}