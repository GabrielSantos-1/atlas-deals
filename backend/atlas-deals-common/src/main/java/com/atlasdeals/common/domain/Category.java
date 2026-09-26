package com.atlasdeals.common.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Category extends ValueObject {
    private final String name;
    private final String slug;
    private final Category parent;
    private final List<Category> children;

    private Category(String name, String slug, Category parent, List<Category> children) {
        this.name = Objects.requireNonNull(name, "Category name must not be null");
        this.slug = Objects.requireNonNull(slug, "Category slug must not be null");
        this.parent = parent;
        this.children = children != null ? Collections.unmodifiableList(new ArrayList<>(children)) : List.of();
    }

    public static Category of(String name, String slug) {
        return new Category(name, slug, null, List.of());
    }

    public static Category of(String name, String slug, Category parent) {
        return new Category(name, slug, parent, List.of());
    }

    public Category withChild(Category child) {
        List<Category> newChildren = new ArrayList<>(this.children);
        newChildren.add(child);
        return new Category(this.name, this.slug, this.parent, newChildren);
    }

    public String getFullPath() {
        if (parent == null) {
            return name;
        }
        return parent.getFullPath() + " > " + name;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public Category getParent() { return parent; }
    public List<Category> getChildren() { return children; }

    public boolean isRoot() { return parent == null; }
    public boolean isLeaf() { return children.isEmpty(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return slug.equals(category.slug);
    }

    @Override
    public int hashCode() {
        return slug.hashCode();
    }

    @Override
    public String toString() {
        return getFullPath();
    }
}
