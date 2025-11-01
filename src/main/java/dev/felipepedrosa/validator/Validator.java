package dev.felipepedrosa.validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Validator<T> {

    private final List<Rule<T>> rules;
    private final ValidationStrategy strategy;

    private Validator(List<Rule<T>> rules, ValidationStrategy strategy) {
        this.rules = rules;
        this.strategy = strategy;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public List<String> validate(T target) {
        return strategy.validate(rules, target);
    }

    public static class Builder<T> {
        private final List<Rule<T>> rules = new ArrayList<>();
        private ValidationStrategy strategy = new CollectErrorsValidationStrategy(); // default

        public Builder<T> rule(String message, Predicate<T> predicate) {
            rules.add(new Rule<>(message, predicate));
            return this;
        }

        public Builder<T> failFast() {
            this.strategy = new FailFastValidationStrategy();
            return this;
        }

        public Builder<T> collectAll() {
            this.strategy = new CollectErrorsValidationStrategy();
            return this;
        }

        public Validator<T> build() {
            return new Validator<>(rules, strategy);
        }
    }

    private record Rule<T>(String message, Predicate<T> predicate) {
    }

    private interface ValidationStrategy {
        <T> List<String> validate(List<Rule<T>> rules, T target);
    }

    private static class FailFastValidationStrategy implements ValidationStrategy {
        @Override
        public <T> List<String> validate(List<Rule<T>> rules, T target) {
            return rules.stream()
                    .filter(rule -> !rule.predicate().test(target))
                    .findFirst()
                    .map(rule -> List.of(rule.message))
                    .orElse(Collections.emptyList());
        }
    }

    private static class CollectErrorsValidationStrategy implements ValidationStrategy {
        @Override
        public <T> List<String> validate(List<Rule<T>> rules, T target) {
            return rules.stream()
                    .filter(rule -> !rule.predicate().test(target))
                    .map(Rule::message)
                    .toList();
        }
    }
}