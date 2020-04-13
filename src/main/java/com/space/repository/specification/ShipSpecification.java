package com.space.repository.specification;

import com.space.model.Ship;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShipSpecification implements Specification<Ship> {
    List<Condition> conditions;

    public ShipSpecification() {
        this.conditions = new ArrayList<>();
    }

    public void addCondition(Condition condition) {
        this.conditions.add(condition);
    }

    public int getSize() {
        return conditions.size();
    }
    @Override
    public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        for(Condition condition : conditions) {
            if (condition.coparison == Coparison.LIKE) {
                predicates.add(criteriaBuilder.like(root.get(condition.field), "%" + condition.value1 + "%"));
            } else if (condition.coparison == Coparison.BETWEEN && condition.type == Type.DATE) {
                predicates.add(criteriaBuilder.between(root.get(condition.field), (Date) condition.value1, (Date) condition.value2));
            } else if (condition.coparison == Coparison.BETWEEN && condition.type == Type.INTEGER) {
                predicates.add(criteriaBuilder.between(root.get(condition.field), (Integer) condition.value1, (Integer) condition.value2));
            } else if (condition.coparison == Coparison.BETWEEN && condition.type == Type.DOUBLE) {
                predicates.add(criteriaBuilder.between(root.get(condition.field), (Double) condition.value1, (Double) condition.value2));
            } else if (condition.coparison == Coparison.EQUAL) {
                predicates.add(criteriaBuilder.equal(root.get(condition.field), condition.value1));
            }
        }
        return predicates.size() > 1
                ? criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]))
                : predicates.get(0);
    }

}
