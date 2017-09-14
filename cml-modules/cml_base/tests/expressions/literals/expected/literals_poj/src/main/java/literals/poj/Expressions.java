package literals.poj;

import java.util.*;
import java.math.*;
import org.jetbrains.annotations.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

public class Expressions
{
    private final boolean literalTrueBoolean;
    private final boolean literalFalseBoolean;
    private final String literalStringInit;
    private final int literalIntegerInit;
    private final BigDecimal literalDecimalInit;
    private final BigDecimal literalDecimalInit2;

    public Expressions()
    {
        this(true, false, "\tSome \"String\"\n", 123, new BigDecimal("123.456"), new BigDecimal(".456"));
    }

    public Expressions(boolean literalTrueBoolean, boolean literalFalseBoolean, String literalStringInit, int literalIntegerInit, BigDecimal literalDecimalInit, BigDecimal literalDecimalInit2)
    {
        this.literalTrueBoolean = literalTrueBoolean;
        this.literalFalseBoolean = literalFalseBoolean;
        this.literalStringInit = literalStringInit;
        this.literalIntegerInit = literalIntegerInit;
        this.literalDecimalInit = literalDecimalInit;
        this.literalDecimalInit2 = literalDecimalInit2;
    }

    public boolean getLiteralTrueBoolean()
    {
        return this.literalTrueBoolean;
    }

    public boolean getLiteralFalseBoolean()
    {
        return this.literalFalseBoolean;
    }

    public String getLiteralStringInit()
    {
        return this.literalStringInit;
    }

    public int getLiteralIntegerInit()
    {
        return this.literalIntegerInit;
    }

    public BigDecimal getLiteralDecimalInit()
    {
        return this.literalDecimalInit;
    }

    public BigDecimal getLiteralDecimalInit2()
    {
        return this.literalDecimalInit2;
    }

    public String toString()
    {
        return new StringBuilder(Expressions.class.getSimpleName())
                   .append('(')
                   .append("literalTrueBoolean=").append(String.format("\"%s\"", getLiteralTrueBoolean())).append(", ")
                   .append("literalFalseBoolean=").append(String.format("\"%s\"", getLiteralFalseBoolean())).append(", ")
                   .append("literalStringInit=").append(String.format("\"%s\"", getLiteralStringInit())).append(", ")
                   .append("literalIntegerInit=").append(String.format("\"%s\"", getLiteralIntegerInit())).append(", ")
                   .append("literalDecimalInit=").append(String.format("\"%s\"", getLiteralDecimalInit())).append(", ")
                   .append("literalDecimalInit2=").append(String.format("\"%s\"", getLiteralDecimalInit2()))
                   .append(')')
                   .toString();
    }
}