package com.space.repository.specification;

public class Condition {
    public Object value1 = null;
    public Object value2 = null;
    public Coparison coparison;
    public String field;
    public Type type;

    public Condition(Object value1, Object value2, Coparison coparison, String field, Type type) {
        this.value1 = value1;
        this.value2 = value2;
        this.coparison = coparison;
        this.field = field;
        this.type = type;
    }
}
