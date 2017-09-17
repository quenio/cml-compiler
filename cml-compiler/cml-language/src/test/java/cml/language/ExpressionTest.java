package cml.language;

import cml.io.Console;
import cml.io.FileSystem;
import cml.language.expressions.Expression;
import cml.language.features.Concept;
import cml.language.features.Module;
import cml.language.foundation.Model;
import cml.language.foundation.Property;
import cml.language.loader.ModelLoader;
import cml.language.types.Type;
import cml.templates.ModelAdaptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static cml.language.functions.ModelFunctions.moduleOf;
import static cml.language.functions.ModuleFunctions.conceptOf;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class ExpressionTest
{
    private static final String BASE_PATH = "./src/test/resources/cml/language/ExpressionTest";
    private static final String ENCODING = "UTF-8";
    private static final char START_CHAR = '<';
    private static final char STOP_CHAR = '>';

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> modulePaths()
    {
        final File file = new File(BASE_PATH);
        final File[] files = file.listFiles(File::isDirectory);
        
        return stream(files == null ? new File[0] : files)
                .map(f -> new Object[] { f.getName(), f })
                .collect(toList());
    }

    private final File modulePath;
    private final FileSystem fileSystem;
    private final ModelLoader modelLoader;
    private final STGroupFile groupFile;

    public ExpressionTest(@SuppressWarnings("unused") String moduleName, File modulePath)
    {
        this.modulePath = modulePath;
        this.fileSystem = FileSystem.create(Console.createSystemConsole());
        this.modelLoader = ModelLoader.create(Console.createSystemConsole(), fileSystem);
        this.groupFile = getOclTemplateGroup();
    }

    @Test
    public void expressionOCL() throws Exception
    {
        final Concept concept = loadExpressions();
        final Properties expectedOCL = loadProperties("expected_ocl.properties");

        for (final Property property: concept.getProperties())
        {
            assertExpectedOCL(expectedOCL, property);
        }
    }

    @Test
    public void expectedType() throws Exception
    {
        final Concept concept = loadExpressions();
        final Properties expectedType = loadProperties("expected_type.properties");

        for (final Property property: concept.getProperties())
        {
            assertExpectedType(expectedType, property);
        }
    }

    private Concept loadExpressions()
    {
        final Model model = Model.create();
        modelLoader.loadModel(model, modulePath.getPath());

        final String moduleName = modulePath.getName();
        final Optional<Module> module = moduleOf(model, moduleName);
        assertTrue("Module should be found: " + moduleName, module.isPresent());

        final Optional<Concept> concept = conceptOf(module.get(), "Expressions");
        assertTrue("The Expressions concept should be found in module: " + moduleName, concept.isPresent());

        return concept.get();
    }

    private Properties loadProperties(String fileName) throws IOException
    {
        final Properties properties = new Properties();
        final File propertiesFile = new File(modulePath, fileName);

        try (final FileInputStream fileInputStream = new FileInputStream(propertiesFile))
        {
            properties.load(fileInputStream);
        }

        return properties;
    }

    private static STGroupFile getOclTemplateGroup()
    {
        final STGroupFile groupFile = new STGroupFile(
            BASE_PATH + File.separator + "ocl.stg",
            ENCODING, START_CHAR, STOP_CHAR);

        groupFile.registerModelAdaptor(Object.class, new ModelAdaptor());

        return groupFile;
    }

    private void assertExpectedOCL(Properties expectedOCL, Property property)
    {
        final String expectedOCLExpression = expectedOCL.getProperty(property.getName());

        if (expectedOCLExpression == null)
        {
            assertFalse(
                "Expected non-init property or missing property in expected_ocl.properties file: " + property.getName(),
                property.getValue().isPresent());
        }
        else
        {
            assertTrue("Expected value for property: " + property.getName(), property.getValue().isPresent());

            final ST oclTemplate = groupFile.getInstanceOf("ocl");
            oclTemplate.add("expr", property.getValue().get());

            assertEquals(
                "Property should match OCL: " + property.getName(),
                expectedOCLExpression,
                oclTemplate.render());
        }
    }

    private void assertExpectedType(Properties expectedTypes, Property property)
    {
        final String expectedType = expectedTypes.getProperty(property.getName());

        if (expectedType == null)
        {
            assertFalse(
                "Expected non-init property or missing property in expected_type.properties file: " + property.getName(),
                property.getValue().isPresent());
        }
        else 
        {
            assertTrue("Expected type for property: " + property.getName(), property.getValue().isPresent());

            final Expression value = property.getValue().orElse(null);
            assertNotNull("Should have init for property: " + property.getName(), value);

            final Type type = value.getType();
            assertNotNull("Should have computed type for property: " + property.getName(), type);

            if (type.getErrorMessage().isPresent())
            {
                fail("NamedType Error of property '" + property.getName() + "': " + type.getErrorMessage().get());
            }

            assertEquals(
                "Property should match expected type: " + property.getName(),
                expectedType,
                type.toString());
        }
    }
}
