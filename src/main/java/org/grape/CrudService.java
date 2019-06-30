package org.grape;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public interface CrudService<T extends BaseDomain> {
    void insert(T domain);

    void update(T domain);

    void save(T domain);

    boolean delete(T domain);

    boolean deletePermanent(T domain);

    void deleteById(String id);

    @NonNull
    List<T> all();

    Optional<T> findById(String id);

    @Nullable
    T findByIdNullable(String id);

    PagedResultList<T> find(SimpleQuery cond);
}
