package com.pipeline.datapipeline.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.*;
import jdk.jshell.execution.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.bcel.Const;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class DataBeanGenerator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String packageName = "com.pipeline.datapipeline.beans";
    private static final String beanBaseName = "DataBean";

    // Create a temporary directory
    private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));

    // Load the compiled class
    private static URLClassLoader classLoader;

    static {
        try {
            classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toURI().toURL()});
        } catch (MalformedURLException e) {
            LOGGER.error("URLClassLoader could not be created");
            e.printStackTrace();
        }
    }


    public static List<MethodSpec> generateBeanClass(JsonNode schema, String apiName) throws IOException {
        // Create a new Java class
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(apiName+beanBaseName)
                .addModifiers(Modifier.PUBLIC);

        MethodSpec.Builder constuctorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        classBuilder.addMethod(constuctorBuilder.build());

        // Loop through the schema to create fields
        List<MethodSpec> getterSetterMethods = new ArrayList<>();
        processSchemaFields(classBuilder, schema, Constants.BUILDER_ROOT, getterSetterMethods);

        for (MethodSpec method : getterSetterMethods) {
            classBuilder.addMethod(method);
        }

        // Generate a toString method
        MethodSpec toStringMethod = generateToStringMethod(getterSetterMethods, apiName);
        classBuilder.addMethod(toStringMethod);


        // Create the Java file and write it to a file
        JavaFile.Builder javaFileBuilder = JavaFile.builder(packageName, classBuilder.build());
        JavaFile javaFile = javaFileBuilder.build();
        javaFile.writeTo(System.out);

        // Get the generated Java code as a String
        String generatedCode = javaFile.toString();

        String fileName = apiName+beanBaseName+".java";

        // Create the temporary file with the desired name
        File tempFile = new File(tempDir+"/"+packageName.replace(".", "/"), fileName);
        tempFile.getParentFile().mkdirs();

        // Save the generated code to a temporary file
        java.nio.file.Files.write(tempFile.toPath(), generatedCode.getBytes());

        // Compile the generated code
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int compilationResult = compiler.run(null, null, null, tempFile.getPath());

        if (compilationResult == 0) {
            LOGGER.info(fileName + " compilation was successful.");
        } else {
            LOGGER.error(fileName + " compilation failed.");
        }

        tempFile.deleteOnExit();

        return getterSetterMethods;
    }

    private static void processSchemaFields(TypeSpec.Builder classBuilder, JsonNode schema, String fieldName, List<MethodSpec> getterSetterMethods) {
        if (schema.has("type") && schema.get("type").asText().equals("object")) {
            TypeSpec.Builder nestedClassBuilder = TypeSpec.classBuilder(fieldName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

            // Create a default (no-argument) constructor
            MethodSpec.Builder constuctorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);
            constuctorBuilder.addParameter(ClassName.bestGuess(fieldName), Constants.BUILDER_VARIABLE);

            JsonNode properties = schema.get("properties");
            for (Iterator<Map.Entry<String, JsonNode>> it = properties.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                String propertyName = entry.getKey();
                JsonNode propertySchema = entry.getValue();
                String propertyType = propertySchema.get("type").asText();

                ClassName propertyClassName = ClassName.get(mapJsonTypeToJavaType(propertyType));

                FieldSpec propertyField = FieldSpec.builder(propertyClassName, propertyName, Modifier.PRIVATE).build();
                nestedClassBuilder.addField(propertyField);
                classBuilder.addField(propertyField);

                String getterMethodName = "get" + StringManipulation.toCamelCase(propertyName);
                String setterMethodName = "set" + StringManipulation.toCamelCase(propertyName);
                String builderMethodName = "with" + StringManipulation.toCamelCase(propertyName);


                MethodSpec getterMethod = MethodSpec.methodBuilder(getterMethodName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(propertyClassName)
                        .addStatement("return $L", propertyName)
                        .build();
                getterSetterMethods.add(getterMethod);

                MethodSpec setterMethod = MethodSpec.methodBuilder(setterMethodName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(propertyClassName, propertyName)
                        .addStatement("this.$L = $L", propertyName, propertyName)
                        .build();
                getterSetterMethods.add(setterMethod);

                MethodSpec nestedMethod = MethodSpec.methodBuilder(builderMethodName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.bestGuess(Constants.BUILDER_ROOT))
                        .addParameter(propertyClassName, propertyName)
                        .addStatement("this.$L = $L", propertyName, propertyName)
                        .addStatement("return this")
                        .build();
                nestedClassBuilder.addMethod(nestedMethod);

                constuctorBuilder.addStatement("this.$L = $L.$L", propertyName, Constants.BUILDER_VARIABLE, propertyName);
            }

            MethodSpec constructor = constuctorBuilder.build();
            classBuilder.addMethod(constructor);


            // Generate a builder method
            MethodSpec builderMethod = generateBuilderMethod(classBuilder.build());
            nestedClassBuilder.addMethod(builderMethod);

            classBuilder.addType(nestedClassBuilder.build());
        }
    }

    private static Class mapJsonTypeToJavaType(String jsonType) {
        switch (jsonType) {
            case "string":
                return String.class;
            case "integer":
                return Integer.class;
            case "number":
                return Double.class;
            case "char":
                return Character.class;
            case "boolean":
                return Boolean.class;
            default:
                return String.class;
        }
    }

    private static MethodSpec generateToStringMethod(List<MethodSpec> getterSetterMethods, String apiName) {
        MethodSpec.Builder toStringBuilder = MethodSpec.methodBuilder("toString")
                .addModifiers(Modifier.PUBLIC)
                .returns(String.class);

        StringBuilder toStringCode = new StringBuilder("return \"" + apiName + "DataBean {\\n\" +\n");

        for (MethodSpec getter : getterSetterMethods) {
            if (getter.name.startsWith("get")) {
                String propertyName = getter.name.substring(3); // Remove "get" prefix
                toStringCode.append("\"  ").append(propertyName).append(" = \" + ").append(getter.name).append("() + \"\\n\" +\n");
            }
        }

        toStringCode.append("\"}\";");
        toStringBuilder.addCode(toStringCode.toString());

        return toStringBuilder.build();
    }

    private static MethodSpec generateBuilderMethod(TypeSpec mainClass) {
        ClassName builderClassName = ClassName.get(packageName, mainClass.name);
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(builderClassName)
                .addStatement("return new $T(this)", builderClassName)
                .build();
    }

    public static Object buildDataEntryObject(String apiName) {
        Object dataEntry = null;
        try {
            Class<?> generatedClass = Class.forName(packageName+"." + apiName + beanBaseName, true, classLoader);
            dataEntry = generatedClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("Error creating an instance of " + apiName + beanBaseName);
            e.printStackTrace();
        }

        return dataEntry;
    }

}
