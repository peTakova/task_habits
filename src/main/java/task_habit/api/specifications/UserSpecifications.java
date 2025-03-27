package task_habit.api.specifications;

import task_habit.api.model.User;
import task_habit.api.model.User_;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

public class UserSpecifications {

    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (email != null && !email.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get(User_.email), email));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasUsername(String name) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get(User_.username), name));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

}
