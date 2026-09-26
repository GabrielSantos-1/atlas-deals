package com.atlasdeals.common.domain.specification;

import java.util.function.Predicate;

@FunctionalInterface
public interface Specification<T> extends Predicate<T> {

    boolean isSatisfiedBy(T candidate);

    @Override
    default boolean test(T candidate) {
        return isSatisfiedBy(candidate);
    }

    default Specification<T> and(Specification<? super T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<? super T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
