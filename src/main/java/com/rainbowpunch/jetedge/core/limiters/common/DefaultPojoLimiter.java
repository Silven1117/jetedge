package com.rainbowpunch.jetedge.core.limiters.common;

import com.rainbowpunch.jetedge.core.PojoAttributes;
import com.rainbowpunch.jetedge.core.limiters.Limiter;
import com.rainbowpunch.jetedge.core.reflection.ClassAttributes;
import com.rainbowpunch.jetedge.spi.PojoGenerator;
import com.rainbowpunch.jetedge.spi.PojoGeneratorBuilder;

import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DefaultPojoLimiter<T> implements Limiter<T> {

    private final PojoGenerator<T> generator;

    // TODO: 12/5/17 Resolve generics to only require classAttributes instead of it and class
    public DefaultPojoLimiter(ClassAttributes parentClassAttributes, Class<T> clazz, PojoAttributes<T> parentAttributes) {
        PojoGeneratorBuilder<T> builder = new PojoGeneratorBuilder<>(clazz,
                parentAttributes.getRandomSeed(), parentAttributes.getParentPojoAnalyzer());

        parentAttributes.getLimiters().entrySet().stream()
                .forEach(entry -> addToBuilder(entry, builder));

        parentAttributes.getAllFieldLimiterMap().entrySet().stream()
                .map(Map.Entry::getValue)
                .forEach(builder::andLimitAllFieldsOf);

        parentAttributes.getFieldsToIgnore().stream()
                .forEach(builder::andIgnoreField);

        generator = builder.build(parentClassAttributes);
    }

    private boolean shouldProcess(Map.Entry<Class, Map<String, Limiter<?>>> entry, PojoAttributes parentAttributes) {
        Class clazz = entry.getKey();
        return !clazz.equals(parentAttributes.getPojoClazz());
    }

    private void addToBuilder(Map.Entry<String, Limiter<?>> entry, PojoGeneratorBuilder builder) {
        builder.andLimitField(entry.getKey(), entry.getValue());
    }

    private Stream<Map.Entry<String, Limiter<?>>> flattenLimiterMap(Map.Entry<Class, Map<String, Limiter<?>>> entry) {
        return entry.getValue()
                .entrySet()
                .stream();
    }

    @Override
    public Supplier<T> generateSupplier(Random random) {
        return generator::generatePojo;
    }
}
