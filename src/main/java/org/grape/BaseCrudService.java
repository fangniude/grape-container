package org.grape;

import io.ebean.PagedList;
import io.ebean.Query;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public abstract class BaseCrudService<T extends BaseDomain> implements CrudService<T> {
    protected final BaseDomain.Finder<T> finder;
    protected final Class<T> domainClass;

    public BaseCrudService(Class<T> domainClass) {
        this.domainClass = domainClass;
        finder = new BaseDomain.Finder<>(domainClass);
    }

    @Override
    public void insert(T domain) {
        domain.insert();
    }

    @Override
    public void update(T domain) {
        domain.update();
    }

    @Override
    public void save(T domain) {
        domain.save();
    }

    @Override
    public boolean delete(T domain) {
        return domain.delete();
    }

    @Override
    public boolean deletePermanent(T domain) {
        return domain.deletePermanent();
    }

    @Override
    public void deleteById(String id) {
        finder.deleteById(id);
    }

    @Override
    public List<T> all() {
        return finder.all();
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(findByIdNullable(id));
    }

    @Nullable
    @Override
    public T findByIdNullable(String id) {
        return finder.byId(id);
    }

    @Override
    public PagedResultList<T> find(SimpleQuery cond) {
        Query<T> query = finder.query();
        PagedList<T> pagedList = query
                .select(String.join(",", cond.getColumns()))
                .where(cond.whereExpression(query))
                .setOrderBy(cond.orderBy())
                .setMaxRows(cond.getPageSize())
                .setFirstRow(cond.offSet())
                .findPagedList();
        return new PagedResultList<T>(pagedList);
    }
}
