package com.rainbowpunch.jtdg.core;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 *
 */
public class DefaultPojoAnalyzer<T> implements PojoAnalyzer<T> {

    private static final Pattern pattern = Pattern.compile("set.*");

    @Override
    public void parsePojo(Class<T> clazz, PojoAttributes<T> attributes) {
        try {
            Method[] methods = clazz.getMethods();

            Arrays.asList(methods)
                    .stream()
                    .map(this::getMethodName)
                    .filter(this::getSetterMethods)
                    .filter(this::removeIgnored)
                    .map(this::getMethodParameters)
                    .forEach(method -> {
                        FieldSetter fieldSetter = FieldSetter.makeFieldSetter(method.getValue());
                        fieldSetter.setConsumer(createBiConsumer(method.getKey()));
                        attributes.putFieldSetter(method.getKey().getName(), fieldSetter);
                    });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map.Entry<Method, String> getMethodName(Method method) {
        return new AbstractMap.SimpleEntry<>(method, method.getName());
    }

    private boolean getSetterMethods(Map.Entry<Method, String> entry) {
        return pattern.matcher(entry.getValue()).find();
    }

    private boolean removeIgnored(Map.Entry<Method, String> entry) {
        return true; // TODO: 7/29/17
    }

    private Map.Entry<Method, Class> getMethodParameters(Map.Entry<Method, String> entry) {
        // TODO: 7/29/17 Throw error if not a simple setter
        return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getKey().getParameterTypes()[0]);
    }

    private BiConsumer<T, ?> createBiConsumer(Method method) {
        return (instance, value) -> {
            try {
                method.invoke(instance, value);
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO: 7/29/17
            }
        };
    }
}
