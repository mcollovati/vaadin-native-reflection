package com.example.checks;

import java.util.Objects;

/** Plain JavaBean used by one smoke check only. */
public class BinderBean {

    private String name;

    public BinderBean() {
    }

    public BinderBean(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof BinderBean other && Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "BinderBean[name=" + name + "]";
    }
}
