package common;

import cml.language.features.Concept;
import cml.language.foundation.NamedElement;
import cml.language.foundation.Property;
import cml.language.foundation.Type;
import com.google.common.io.Resources;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

@RunWith(Parameterized.class)
public abstract class LangTest extends TemplateTest
{
    private static final Charset OUTPUT_FILE_ENCODING = Charset.forName("UTF-8");
    private static final String EXPECTED_OUTPUT_PATH = "/%s/%s/%s";
    private static final String LANG_GROUP_PATH = "lang/%s";

    @Parameters
    public static Collection<String> targetLanguageExtension()
    {
        return asList("java", "py");
    }

    private final String targetLanguageExtension;

    LangTest(String targetLanguageExtension)
    {
        this.targetLanguageExtension = targetLanguageExtension;
    }

    protected abstract String getExpectedOutputPath();

    @Override
    protected String getTemplatePath()
    {
        return format(LANG_GROUP_PATH, targetLanguageExtension);
    }

    void testTemplateWithConcept(String templateName, Concept concept, String expectedOutputPath)
        throws IOException
    {
        testTemplate(templateName, "concept", concept, expectedOutputPath);
    }

    void testTemplateWithProperty(String templateName, Property property, String expectedOutputPath)
        throws IOException
    {
        testTemplate(templateName, "property", property, expectedOutputPath);
    }

    void testTemplateWithType(String templateName, Type type, String expectedOutputPath) throws IOException
    {
        testTemplate(templateName, "type", type, expectedOutputPath);
    }

    void testTemplateWithNamedElement(String templateName, NamedElement namedElement, String expectedOutputPath)
        throws IOException
    {
        testTemplate(templateName, "named_element", namedElement, expectedOutputPath);
    }

    void assertThatOutputMatches(String expectedOutputPath, String actualOutput) throws IOException
    {
        expectedOutputPath = format(EXPECTED_OUTPUT_PATH, targetLanguageExtension, getExpectedOutputPath(), expectedOutputPath);

        final URL expectedOutputResource = getClass().getResource(expectedOutputPath);
        assertNotNull("Expected output resource must exist: " + expectedOutputPath, expectedOutputResource);

        final String expectedOutput = Resources.toString(expectedOutputResource, OUTPUT_FILE_ENCODING);
        assertEquals(expectedOutputPath, expectedOutput, actualOutput);
    }

    private void testTemplate(String templateName, String paramName, Object paramValue, String expectedOutputPath)
        throws IOException
    {
        if (!getExpectedOutputPath().endsWith(templateName))
        {
            expectedOutputPath = templateName + File.separator + expectedOutputPath;
        }

        final ST template = getTemplate(templateName);
        assertNotNull("Expected template: " + templateName, template);

        template.add(paramName, paramValue);

        final String result = template.render();

        assertThatOutputMatches(expectedOutputPath, result);
    }
}